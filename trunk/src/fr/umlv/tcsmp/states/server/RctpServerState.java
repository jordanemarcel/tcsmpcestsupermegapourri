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
	
	private String currentRCPTDomain;
	private Protocol fakeProto;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		/** we are in a relaying mode
		 */
		if (fakeProto != null) {
			
			// get response from the fake proto.
			Response res = fakeProto.doIt(bb);
		
			// reply code to the client
			if (res.getAction() == ResponseAction.READ && send == false) {
				fakeProto = null;
				bb.position(0);
				return new Response(ResponseAction.WRITE);
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
		
		String dest = null;
		try {
			dest = TCSMPParser.parseDomain(args[1]);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode("501 Not a valid address."));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		if (proto.isRelay(dest) == false) {
			bb.put(TCSMPParser.encode("250 OK\r\n"));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		/**
		 * Check address
		 */
		try {
			String domain = TCSMPParser.parseDomain(args[1]);
			String user = TCSMPParser.parseUser(args[1]);
			proto.getRecpts().add(user + "@" + domain);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode(new String("500 Invalid RCPT.\r\n")));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		/**
		 * Create a fakeProto
		 */
		fakeProto = proto.newProtocol();
		fakeProto.setState(new TeloClientState());
		currentRCPTDomain = dest;
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ)
			return new Response(currentRCPTDomain, ResponseAction.WRITE);
		return res;
	}
}
