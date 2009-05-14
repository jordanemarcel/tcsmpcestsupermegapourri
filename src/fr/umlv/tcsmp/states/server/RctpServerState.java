package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.states.client.RctpClientState;
import fr.umlv.tcsmp.states.client.TeloClientState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class RctpServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes
	
	private boolean send = false;
	private boolean error = false;

	private String currentRCPTDomain;
	private String serverResponse;
	private Protocol fakeProto;

	public RctpServerState() {
		super(TIMEOUT);
	}
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {

		// we are in a relaying mode
		if (fakeProto != null) {
			
			// write an error response to the client and stop relaying
			if (error) {
				
			}

			// save each time the response of the server
			serverResponse = TCSMPParser.decode(bb);
			
			// get response from the fake proto.
			Response res = fakeProto.doIt(bb);

			// reply code to the client
			if (send == false) {
				if (res.getAction() == ResponseAction.WRITE) {
					fakeProto = null;
					bb.clear();
					bb.put(TCSMPParser.encode(serverResponse));
					bb.flip();
					return new Response(ResponseAction.WRITE);
				}
				else {
					return new Response(currentRCPTDomain, ResponseAction.READ);
				}
			}

			// last state, the next response will be replied to the client
			if (fakeProto.getState().getClass().equals(RctpClientState.class)) {
				send = false;
			}

			// override response in order to add the currentRCPTDomain
			if (res.getAction() != ResponseAction.READ) {
				return new Response(currentRCPTDomain, ResponseAction.WRITE);
			}

			return new Response(currentRCPTDomain, ResponseAction.READ);
		}
		
		// are we in timeout waiting for RCTP
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		if (send) {
			send = false;
			bb.clear();
			return new Response(ResponseAction.READ);
		}

		send = true;

		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}

		if (args.length > 2 || (args[0].equals("RCPT") == false && args[0].equals("APZL") == false)) {
			bb.put(ErrorReplies.unknowCommand("RCPT|APZL", args[0]));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}

		// manual switch state if needed
		if (args[0].equals("APZL")) {
			TCSMPState apzlState = new ApzlServerState();
			proto.setState(apzlState);
			// XXX: really need to recreate cmd ?
			bb.put(TCSMPParser.encode("APZL\r\n"));
			bb.flip();
			return apzlState.processCommand(proto, bb);
		}


		// Check address and add it to the rctps array
		String domain;
		try {
			domain = TCSMPParser.parseDomain(args[1]);
			String user = TCSMPParser.parseUser(args[1]);
			proto.getRecpts().add(user + "@" + domain);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode(new String("500 Invalid RCPT.\r\n")));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}

		if (proto.isRelay(domain) == false) {
			bb.put(TCSMPParser.encode("250 OK\r\n"));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}


		// Create a fakeProto for our client states
		fakeProto = proto.newProtocol();
		fakeProto.setState(new TeloClientState());
		currentRCPTDomain = domain;
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ)
			return new Response(currentRCPTDomain, ResponseAction.WRITE);

		return new Response(currentRCPTDomain, ResponseAction.READ);
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		error = false;
		fakeProto = null;
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
