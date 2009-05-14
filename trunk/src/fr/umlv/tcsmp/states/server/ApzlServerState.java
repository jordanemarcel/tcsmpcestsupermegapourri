package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
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

		// response has been sent... OK.
		if (send) {
			if (error == false)
				proto.setState(new MailServerState());
			else
				send = error = false;
			bb.clear();
			return new Response(ResponseAction.READ);
		}

		// we wait for response from domains
		if (domains != null || responses != null) {

			// first call, we have to read response from the current domain
			if (responses == null) {			
				responses = new ArrayList<String>();
				return new Response(currentDomain, ResponseAction.READ);
			}

			// we have a response for the currentDomain
			if (domains.size() != 0) {
				// add its response
				// XXX: split multiline response
				responses.add(TCSMPParser.decode(bb));
				domains.remove(currentDomain);
				try {
					currentDomain = domains.getFirst();
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

			// we are currently writing response to the client
			if (responses.size() != 0) {
				String res = responses.remove(0);
				bb.clear();
				bb.put(TCSMPParser.encode(res));
				bb.flip();
				// yeah we have finished
				if (responses.size() == 0) {
					send = true;
				}
				return new Response(ResponseAction.WRITE);
			}

			throw new AssertionError("response and domains list should not be empty");
		}

		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();


		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}

		if (args.length != 1 || args[0].equals("APZL") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("APZL", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		// I'm concerned.
		if (proto.isRelay() == false) {
			// XXX: puzzle size must be dynamic
			Puzzle p = Puzzle.randomPuzzle(4, 4);
			Puzzle.shuffle(p);
			proto.addPuzzleFor(proto.getClientDomain(), p);
			bb.clear();
			bb.put(TCSMPParser.encode("215 " + proto.getMyDomains().get(0) + " 4,4 " + p.lineString() + "\r\n"));
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}

		// create a list with all the domain of the rctp
		domains = new LinkedList<String>();
		for (String r : proto.getRecpts()) {
			try {
				domains.add(TCSMPParser.parseDomain(r));
			} catch (ParseException e) {
			}
		}

		// set the current processed domain
		currentDomain = domains.getFirst();

		// reset cmd
		bb.position(0);

		// send it to any RCPT
		return new Response(ResponseAction.RELAYALL);
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
