package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class PkeyState implements TCSMPState {

	public Response processCommand(Protocol proto, ByteBuffer bb) {

		String [] args = TCSMPParser.parse(bb);
		if (args.length != 4 || args[0].equals("PKEY") == false) {
			return new Response(ErrorReplies.unknowCommand("PKEY", args[0]));
		}

		/**
		 * check to see if it's a solution for us or not
		 * XXX: foo
		 */
		if (args[1].equals("mydomain")) {
			/*
			 * XXX: We are asuming that puzzle solution is right 
			 */
			ByteBuffer response = ByteBuffer.wrap("216 Your mail has been kept !\r\n".getBytes());
			return new Response(response);
		}
		
		/**
		 * We have to forward the MAIL to the server before
		 * sending the PKEY command.
		 * XXX: create states especially for this ?
		 */
		return null;
	}

}
