package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPLogger;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes
	
	private boolean send = false;
	private boolean error = false;
	
	public TeloServerState() {
		super(TIMEOUT);
	}
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		if (send) {
			if (error == false)
				proto.setState(new FromServerState());
			else
				error = send = false;
			bb.clear();
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
		
		if (args.length != 2 || args[0].equals("TELO") == false) {
			bb.put(ErrorReplies.unknowCommand("TELO", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		// check domain
		if (args[1].contains(".") == false) {
			bb.put(TCSMPParser.encode("500 Invalid domain syntax.\r\n"));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}
		
		// OK write domain in the proto
		proto.setClientDomain(args[1]);
		
		/**
		 * Create response buffer.
		 */
		bb.put(TCSMPParser.encode("250-TCSMPv1\r\n250 OK greets " + args[1] + "\r\n"));
		bb.flip();
		
		return new Response(ResponseAction.WRITE);
	}
}
