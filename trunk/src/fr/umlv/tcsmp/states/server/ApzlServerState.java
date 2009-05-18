package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ApzlServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes

	private boolean send = false;
	private boolean error = false;
	private boolean relayed = false;
	private boolean waitreply = false;
	private boolean sendresponse = false;

	// current response proccessed domain
	private String currentDomain;

	// domains not treated
	private LinkedList<String> domains;

	// string of responses to the apzl command
	private ArrayList<String> responses;

	public ApzlServerState() {
		super(TIMEOUT);
	}

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {

		// relayed
		if (relayed == true) {
			relayed = false;
			waitreply = true;
			bb.clear();
			return new Response(currentDomain, ResponseAction.READ);
		}
		
		// wait reply from domain
		if (waitreply == true) {
			waitreply = false;
			responses.addAll(Arrays.asList(TCSMPParser.slipResponseLine(TCSMPParser.decode(bb))));
			try {
				currentDomain = domains.remove(0);
			}
			catch (Exception e) {
				// last domain has been processed
				TCSMPParser.multilinize(responses);
				sendresponse = true;
				bb.clear();
				bb.put(TCSMPParser.encode(responses.remove(0)));
				bb.flip();
				if (responses.size() == 0) {
					send = true;
				}
				return new Response(ResponseAction.WRITE);
			}
		}
		
		if (sendresponse == true) {
			bb.clear();
			bb.put(TCSMPParser.encode(responses.remove(0)));
			bb.flip();
			if (responses.size() == 0) {
				sendresponse = false;
				send = true;
			}
			return new Response(ResponseAction.WRITE);
		}
		
		// response has been sent... OK.
		if (send) {
			if (error == false)
				proto.setState(new MailServerState());
			else
				send = error = false;
			bb.clear();
			return new Response(ResponseAction.READ);
		}

		//		// we wait for response from domains
		//		if (domains != null || responses != null) {
		//
		//			// first call, we have to read response from the current domain
		//			if (responses == null) {			
		//				responses = new ArrayList<String>();
		//				bb.clear();
		//				return new Response(currentDomain, ResponseAction.READ);
		//			}
		//
		//			// we have a response for the currentDomain
		//			if (domains.size() != 0) {
		//				// add its response
		//				responses.addAll(Arrays.asList(TCSMPParser.slipResponseLine(TCSMPParser.decode(bb))));
		//				domains.remove(currentDomain);
		//				try {
		//					currentDomain = domains.getFirst();
		//					bb.clear();
		//					return new Response(currentDomain, ResponseAction.READ);
		//				} catch (Exception e) {
		//					// seems there is no more domains, reply response to the
		//					// client
		//					TCSMPParser.multilinize(responses);
		//					String res = responses.remove(0);
		//					bb.clear();
		//					bb.put(TCSMPParser.encode(res));
		//					bb.flip();
		//					if (responses.size() == 0) {
		//						send = true;
		//					}
		//					return new Response(ResponseAction.WRITE);
		//				}
		//			}
		//
		//			// we are currently writing response to the client
		//			if (responses.size() != 0) {
		//				String res = responses.remove(0);
		//				bb.clear();
		//				bb.put(TCSMPParser.encode(res));
		//				bb.flip();
		//				// yeah we have finished
		//				if (responses.size() == 0) {
		//					send = true;
		//				}
		//				return new Response(ResponseAction.WRITE);
		//			}
		//
		//			throw new AssertionError("response and domains list should not be empty");
		//		}

		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();


		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 0) {
			bb.clear();
			bb.put(ErrorReplies.syntaxError());
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		if (args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}		

		if (args[0].equals("APZL") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("APZL", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		// I'm concerned.
		if (proto.isRelay() == false) {
			int dim = 3 + proto.getRecpts().size();
			Puzzle p = Puzzle.randomPuzzle(dim, dim);
			Puzzle.shuffle(p);
			proto.addPuzzleFor(proto.getClientDomain(), p);
			bb.clear();
			bb.put(TCSMPParser.encode("215 " + proto.getMyDomains().get(0) + " " + dim + "," + dim + " " + p.lineString() + "\r\n"));
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}

		if (domains == null) {
			// create a list with all the domain of the rctp
			domains = new LinkedList<String>();
			for (String r : proto.getRecpts()) {
				try {
					// XXX BOOOOOOOOOOO why do you do this to meh. Need only one appearance 
					// of a domain even if two RCPTs have the same!
					String domain = TCSMPParser.parseDomain(r); 
					if (!domains.contains(domain))
						domains.add(domain);
				} catch (ParseException e) {
				}
			}
			responses = new ArrayList<String>();
		}

		currentDomain = domains.remove(0);
		
		bb.clear();
		bb.put(TCSMPParser.encode("APZL\r\n"));
		bb.flip();
		relayed = true;
		
		
		// send it to any RCPT
		return new Response(currentDomain, ResponseAction.WRITE);
	}


	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {

		// we have received a cancel while we asking for PZL
		// add a error message for this domain and switch
		// to the next one
		if (domains != null) {
			responses.add("515 " + currentDomain + " error while asking for puzzle\r\n");
			domains.remove(currentDomain);
			try {
				currentDomain = domains.getFirst();
				bb.clear();
				return new Response(currentDomain, ResponseAction.READ);
			} catch (Exception e) {
				// seems there is no more domains, reply response to the
				// client
				TCSMPParser.multilinize(responses);
				String res = responses.remove(0);
				bb.clear();
				bb.put(TCSMPParser.encode(res));
				bb.flip();
				if (responses.size() == 0) {
					send = true;
				}
				return new Response(ResponseAction.WRITE);
			}
		}

		// unknow error
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
