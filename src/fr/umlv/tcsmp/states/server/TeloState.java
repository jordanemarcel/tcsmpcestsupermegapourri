package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.TCSMPCommandParser;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;

public class TeloState implements TCSMPState {

	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		String [] args = TCSMPCommandParser.parse(bb);
		
		if (args.length != 2 || args[0].equals("TELO") == false) {
			return new Response(ErrorReplies.unknowCommand("TELO", args[0]));
		}

		/**
		 * XXX: Comment va-t-on faire pour changer de state ici ?
		 * Faut passer le protocole en param ? (Ce que je fais là)
		 */
		
		/**
		 * Change state
		 */
		proto.setState(new FromState());
		
		/**
		 * Create response buffer.
		 */
		ByteBuffer response = ByteBuffer.wrap(new String("200 TCSMPv1\r\n200-OK greets " + args[1] + "\r\n").getBytes());
		
		return new Response(response);
	}
}
