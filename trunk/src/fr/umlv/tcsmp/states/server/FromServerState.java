package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class FromServerState extends TCSMPState {

	private boolean send = false;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (send) {
			proto.setState(new RctpServerState());
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
		
		if (args.length != 2 || args[0].equals("FROM") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("FROM", args[0]));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}

		/** 
		 * Set from
		 * XXX: Here we have to check if from is a real address.
		 */
		proto.setFrom(args[1]);
		
		/**
		 * Create response buffer.
		 */
		bb.put(TCSMPParser.encode(new String("250 OK\r\n")));
		bb.flip();
		
		return new Response(ResponseAction.REPLY);
	}
}
