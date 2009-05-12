package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloServerState extends TCSMPState {

	private boolean send = false;
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			proto.setState(new FromServerState());
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
		
		if (args.length != 2 || args[0].equals("TELO") == false) {
			bb.put(ErrorReplies.unknowCommand("TELO", args[0]));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}

		/** 
		 * Change domain
		 */
		proto.setClientDomain(args[1]);
		
		/**
		 * Create response buffer.
		 */
		bb.put(TCSMPParser.encode("250-TCSMPv1\r\n250 OK greets " + args[1] + "\r\n"));
		bb.flip();
		
		return new Response(ResponseAction.REPLY);
	}
}
