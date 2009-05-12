package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class QuitServerState extends TCSMPState {
	
	private boolean send = false;
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			/* XXX: create an exit state ? */
			return new Response(ResponseAction.CLOSE);
		}
		
		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();
		
		if (args[0].equals("QUIT") == false) {
			bb.put(ErrorReplies.unknowCommand("QUIT", args[0]));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * Check if we have to forward the command.
		 */
		if (proto.isRelay() == false) {
			//return new Response("200 OK\r\n".getBytes(), ResponseAction.RELAYALL);
			return null;
		}
		
		/**
		 * Check to see if we have to forward the message ?
		 * 
		 * XXX: on devrait faire une methode dans le proto
		 * qui dit si on est relay ou non d'un domaine. Comme
		 * ca c'est le proto qui connait le domaine sur lequel
		 * on ecoute?
		 */
		
		return null;
	}
	
}
