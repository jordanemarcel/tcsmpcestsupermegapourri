package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloServerState implements TCSMPState {

	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		String [] args = TCSMPParser.parse(bb);
		
		if (args.length != 2 || args[0].equals("TELO") == false) {
			return new Response(ErrorReplies.unknowCommand("TELO", args[0]));
		}

		/**
		 * Change state
		 */
		proto.setState(new FromServerState());
		
		/** 
		 * Change domain
		 * XXX: check domain.
		 */
		proto.setDomain(args[1]);
		
		/**
		 * Create response buffer.
		 */
		ByteBuffer response = ByteBuffer.wrap(new String("200 TCSMPv1\r\n200-OK greets " + args[1] + "\r\n").getBytes());
		
		return new Response(response);
	}
}
