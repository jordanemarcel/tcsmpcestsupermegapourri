package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class FromServerState extends TCSMPState {

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		if (args.length != 2 || args[0].equals("FROM") == false) {
			return new Response(ErrorReplies.unknowCommand("FROM", args[0]));
		}

		/**
		 * Change state
		 */
		proto.setState(new RctpServerState());
		
		/** 
		 * Set from
		 * XXX: Here we have to check if from is a real address.
		 */
		proto.setFrom(args[1]);
		
		/**
		 * Create response buffer.
		 */
		ByteBuffer response = ByteBuffer.wrap(new String("250 OK\r\n").getBytes());
		
		return new Response(response);
	}
}
