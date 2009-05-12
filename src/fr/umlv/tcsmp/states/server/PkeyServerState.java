package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class PkeyServerState extends TCSMPState {

	public Response processCommand(Protocol proto, ByteBuffer bb) {

		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();
		
		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		if (args.length != 4 || args[0].equals("PKEY") == false) {
			bb.put(ErrorReplies.unknowCommand("PKEY", args[0]));
			return new Response(ResponseAction.REPLY);
		}

		/**
		 * check to see if it's a solution for us or not
		 * XXX: foo
		 */
		if (proto.isRelay(args[1]) == false) {
			/*
			 * XXX: We are asuming that puzzle solution is right 
			 */
			bb.put(TCSMPParser.encode("216 Your mail has been kept !\r\n"));
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * We have to forward the MAIL to the server before
		 * sending the PKEY command.
		 * XXX: create states especially for this ?
		 */
		return null;
	}

}
