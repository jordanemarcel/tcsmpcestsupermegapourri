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

	private final static int TIMEOUT = 300000; // 5 minutes
	
	private boolean send = false;
	private boolean error = false;

	public MailServerState() {
		super(TIMEOUT);
	}
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		// send OK ?
		if (send) {
			if (error == false)
				proto.setState(new DataServerState());
			else
				send = error = false;
			bb.clear();
			return new Response(ResponseAction.READ);
		}

		send = true;

		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 0) {
			bb.clear();
			bb.put(ErrorReplies.syntaxError());
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}
		
		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}

		if (args.length != 1 || args[0].equals("MAIL") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("MAIL", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		bb.clear();
		bb.put(TCSMPParser.encode("354 Start mail input; end with <CRLF>.<CRLF>\r\n"));
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
