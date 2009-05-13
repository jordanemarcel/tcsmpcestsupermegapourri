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
			bb.clear();
			return new Response(ResponseAction.CLOSE);
		}
		
		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();
		
		if (args[0].equals("QUIT") == false) {
			bb.put(ErrorReplies.unknowCommand("QUIT", args[0]));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		// Check if we have to forward the command.
		if (proto.isRelay() == false) {
			bb.put(TCSMPParser.encode("250 See you next time~~~~!\r\n"));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		// Reset command and forward
		return new Response(ResponseAction.RELAYALL);
	}
	
}
