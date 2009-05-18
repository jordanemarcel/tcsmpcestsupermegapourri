package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.LinkedList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class QuitServerState extends TCSMPState {
	private final static String reqMessage = "QUIT\r\n";
	private final static String ackMessage = "221 See you next time~~~~!\r\n";
	private final static int TIMEOUT = 60000; // 1 minutes

	private boolean done = false;
	private boolean parsed = false;
	private boolean clientDead = false;

	private ResponseAction relayingAction;
	private String currentDomain;

	// domains not treated
	private LinkedList<String> domains;

	public QuitServerState() {
		super(TIMEOUT);
	}

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();

		if (done) {
			bb.clear();
			return new Response(ResponseAction.CLOSE);
		}

		if (!parsed) {
			parsed = true;
			String [] args = TCSMPParser.parseCommand(bb);

			if (args.length == 0) {
				bb.clear();
				bb.put(ErrorReplies.syntaxError());
				bb.flip();
				return new Response(ResponseAction.WRITE);
			}

			if (args[0].equals("QUIT") == false) {
				bb.clear();
				bb.put(ErrorReplies.unknowCommand(reqMessage, args[0]));
				bb.flip();
				return new Response(ResponseAction.WRITE);
			}		
		}

		if (!proto.isRelay()) {
			done = true;
			bb.clear();
			bb.put(TCSMPParser.encode(ackMessage));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}


		// we are in a relaying mode

		if (domains == null) {
			// create a list with all the domain of the rctp
			domains = new LinkedList<String>();
			for (String r : proto.getRecpts()) {
				try {
					String domain = TCSMPParser.parseDomain(r); 
					if (!domains.contains(domain))
						domains.add(domain);
				} catch (ParseException e) {
					// NOOP
				}
			}
			if (domains.size() == 0) {
				done = true;
				bb.clear();
				bb.put(TCSMPParser.encode(ackMessage));
				bb.flip();
				return new Response(ResponseAction.WRITE);
			}

			relayingAction = ResponseAction.READ;
		}

		switch(relayingAction) {
			case READ:
				// Reset command and forward
				bb.clear();
				bb.put(TCSMPParser.encode(reqMessage));
				bb.flip();
				currentDomain = domains.removeFirst();
				relayingAction = ResponseAction.WRITE;
				return new Response(currentDomain, relayingAction);
	
			case WRITE:
				bb.clear();

				// finished replying the last QUIT cmd to the client
				if (domains.size() == 0) {
					if (!clientDead) { 
						currentDomain = null;
						done = true;
						bb.put(TCSMPParser.encode(ackMessage));
						bb.flip();
						return new Response(ResponseAction.WRITE);
					}
					return new Response(ResponseAction.CLOSE);
				}

				// Read server answer but actually discard it later
				relayingAction = ResponseAction.READ;
				return new Response(currentDomain, relayingAction);
			default:
				throw new AssertionError("What the, somebody hacked my state?!");
		}

	}

	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		// relaying state? ignore the error and go to the next domain
		if (currentDomain != null) {
			relayingAction = ResponseAction.READ;
			// read the next domain
			return this.processCommand(proto, bb);
		}

		// error with the client occurred
		return new Response(ResponseAction.CLOSE);
	}
}
