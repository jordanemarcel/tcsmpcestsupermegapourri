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
		
		String [] args = TCSMPParser.parse(bb);
		
		/**
		 * le processCommand va etre appeler 2 fois une fois
		 * quand le message sera lu et une fois quand le message
		 * sera ecrit, on doit memoriser l'etat dans lequel
		 * on est
		 */
		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		if (args.length != 2 || args[0].equals("TELO") == false) {
			return new Response(ErrorReplies.unknowCommand("TELO", args[0]));
		}

		/** 
		 * Change domain
		 */
		proto.setClientDomain(args[1]);
		
		/**
		 * Create response buffer.
		 */
		ByteBuffer response = ByteBuffer.wrap(new String("200 TCSMPv1\r\n200-OK greets " + args[1] + "\r\n").getBytes());
		
		return new Response(response);
	}
}
