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

enum ApzlState {
	
	WAITDOIT,	// nothing has been done
	SENDOK,		// when everything has been sent
	RELAYED,	// an APZL command has been relayed
	WAITREPLY,	// waiting reply from server
	SENDRES,	// sending responses to clients
}

public class ApzlServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes

	// tell if we can change state or not
	private boolean error = false;
	
	// state of the relaying process
	private ApzlState relayingState;

	// current response proccessed domain
	private String currentDomain;

	// domains not treated
	private LinkedList<String> domains;

	// string of responses to the apzl command
	private ArrayList<String> responses;

	public ApzlServerState() {
		super(TIMEOUT);
		relayingState = ApzlState.WAITDOIT;
	}

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		switch (relayingState) {
		case SENDOK:
			// response PUZL have been sent...
			if (error == false) {
				proto.setState(new MailServerState());
			}
			else
			{
				error = false;
				relayingState = ApzlState.WAITDOIT;
			}
			bb.clear();
			return new Response(ResponseAction.READ);
		case RELAYED:
			// message has been relayed, waiting reply
			relayingState = ApzlState.WAITREPLY;
			bb.clear();
			return new Response(currentDomain, ResponseAction.READ);
		case WAITREPLY:
			// catch reply from the client
			responses.addAll(Arrays.asList(TCSMPParser.slipResponseLine(TCSMPParser.decode(bb))));
			try {
				currentDomain = domains.remove(0);
			}
			catch (Exception e) {
				// last domain has been processed
				TCSMPParser.multilinize(responses);
				relayingState = ApzlState.SENDRES;
				bb.clear();
				bb.put(TCSMPParser.encode(responses.remove(0)));
				bb.flip();
				if (responses.size() == 0) {
					relayingState = ApzlState.SENDOK;
				}
				return new Response(ResponseAction.WRITE);
			}
			// send command to the next domain
			bb.clear();
			bb.put(TCSMPParser.encode("APZL\r\n"));
			bb.flip();
			relayingState = ApzlState.RELAYED;
			return new Response(currentDomain, ResponseAction.WRITE);
		case SENDRES:
			// send responses to the client
			bb.clear();
			bb.put(TCSMPParser.encode(responses.remove(0)));
			bb.flip();
			if (responses.size() == 0) {
				relayingState = ApzlState.SENDOK;
			}
			return new Response(ResponseAction.WRITE);
		}
	
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
			relayingState = ApzlState.SENDOK;
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
			relayingState = ApzlState.SENDOK;
			return new Response(ResponseAction.WRITE);
		}

		// if we are here, it significates that we have to relay APZL command.
		if (domains == null) {
			// create a list with all the domain of the rctp
			domains = new LinkedList<String>();
			for (String r : proto.getRecpts()) {
				try {
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
		relayingState = ApzlState.RELAYED;
		
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
				bb.put(TCSMPParser.encode("APZL\r\n"));
				bb.flip();
				relayingState = ApzlState.RELAYED;
				return new Response(currentDomain, ResponseAction.WRITE);
			} catch (Exception e) {
				// seems there is no more domains, reply response to the
				// client
				TCSMPParser.multilinize(responses);
				String res = responses.remove(0);
				bb.clear();
				bb.put(TCSMPParser.encode(res));
				bb.flip();
				relayingState = ApzlState.SENDRES;
				if (responses.size() == 0) {
					relayingState = ApzlState.SENDOK;
				}
				return new Response(ResponseAction.WRITE);
			}
		}

		// unknow error
		error = true;
		relayingState = ApzlState.SENDOK;
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
