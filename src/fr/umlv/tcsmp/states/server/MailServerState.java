package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class MailServerState extends TCSMPState {
	
	private boolean send = false;
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			proto.setState(new DataServerState());
			return new Response(ResponseAction.READ);
		}
		
		send = true;
		
		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		if (args.length != 1 || args[0].equals("MAIL") == false) {
			return new Response(ErrorReplies.unknowCommand("MAIL", args[0]));
		}

		/**
		 * Check for forwarding or not
		 */
		if (proto.isRelay() == false) {
			ByteBuffer response = ByteBuffer.wrap("354 Start mail input; end with <CRLF>.<CRLF>\r\n".getBytes());
			return new Response(response);
		}
		
		/* relay to all instead */
		return new Response(bb, ResponseAction.RELAYALL);
	}

}
