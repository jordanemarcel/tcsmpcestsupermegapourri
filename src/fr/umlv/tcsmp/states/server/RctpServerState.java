package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.client.RctpClientState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class RctpServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes
	
	private boolean send = false;

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
			
			// save each time the response of the server
			serverResponse = TCSMPParser.decode(bb);
			
//			TCSMPLogger.debug("RCTP STATE: I've received " + serverResponse + " from " + currentRCPTDomain);
			
			// get response from the fake proto.
			Response res = fakeProto.doIt(bb);

			// reply code to the client
			if (send == false) {
				if (res.getAction() == ResponseAction.WRITE) {
					fakeProto = null;
					send = true;
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
//				TCSMPLogger.debug("TCSMP STATE: TELO, PKEY and RCTP have been done");
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


		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		bb.clear();
		
		if (args.length == 0) {
			bb.put(ErrorReplies.syntaxError());
			bb.flip();
			return new Response(ResponseAction.WRITE);
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
		String user;
		try {
			domain = TCSMPParser.parseDomain(args[1]);
			user = TCSMPParser.parseUser(args[1]);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode(new String("500 Invalid address in RCPT.\r\n")));
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}

		if (proto.isRelay(domain) == false) {
			proto.addRcpt(user + "@" + domain);
			bb.put(TCSMPParser.encode("250 OK\r\n"));
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}

//		TCSMPLogger.debug("RCTP STATE: don't know about " + domain + " ... relaying.");
		// Create a fakeProto for our client states
		fakeProto = proto.newProtocol(ProtocolMode.CLIENT);
		fakeProto.clearRecpts();
		
		System.out.println("rcpt add " + user + "@" + domain);
		fakeProto.addRcpt(user + "@" + domain);
		
		currentRCPTDomain = domain;
		if (TCSMPParser.lookupDomain(proto.getRecpts(), domain)) {
			fakeProto.setState(new RctpClientState());
			proto.addRcpt(user + "@" + domain);
			bb.clear();
			fakeProto.doIt(bb);
			return new Response(currentRCPTDomain, ResponseAction.WRITE);
		}
		
		System.out.println("proto add " + user + "@" + domain);
		proto.addRcpt(user + "@" + domain);
		
		fakeProto.setState(new BannerClientState());
		// init banner client state
		fakeProto.doIt(bb);
		return new Response(currentRCPTDomain, ResponseAction.READ);
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		fakeProto = null;
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
