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

	private boolean send = false;
	
	private String currentRCPT;
	private String currentRCPTDomain;
	private Protocol fakeProto;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		/** we are in a relaying mode
		 */
		if (fakeProto != null) {
			
			/* last state, we have to reply the response to the client */
			if (fakeProto.getState().getClass().equals(RctpClientState.class)) {
				fakeProto = null;
				send = false;
				return new Response(ResponseAction.REPLY);
			}
			
			Response res = fakeProto.doIt(bb);
			
			if (res.getAction() != ResponseAction.READ)
				return new Response(currentRCPTDomain, ResponseAction.RELAY);
			return res;
		}
		
		if (send) {
			send = false;
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
			return new Response(ResponseAction.REPLY);
		}
		
		if (args[0].equals("APZL")) {
			/* bypass normal proccessings */
			TCSMPState apzlState = new ApzlServerState();
			proto.setState(apzlState);
			/* XXX: really need to recreate cmd ? */
			bb.put(TCSMPParser.encode("APZL\r\n"));
			bb.flip();
			return apzlState.processCommand(proto, bb);
		}
		
		String dest = null;
		try {
			dest = TCSMPParser.parseDomain(args[1]);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode("501 Not a valid address."));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		if (proto.isRelay(dest) == false) {
			bb.put(TCSMPParser.encode("250 OK\r\n"));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * Check address
		 */
		try {
			String domain = TCSMPParser.parseDomain(args[1]);
			String user = TCSMPParser.parseUser(args[1]);
			proto.getRecpts().add(user + "@" + domain);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode(new String("500 Invalid from.\r\n")));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * Create a fakeProto
		 */
		fakeProto = proto.newProtocol();
		fakeProto.setState(new TeloClientState());
		currentRCPT = args[1];
		currentRCPTDomain = dest;
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ)
			return new Response(currentRCPTDomain, ResponseAction.RELAY);
		return res;
	}
}
