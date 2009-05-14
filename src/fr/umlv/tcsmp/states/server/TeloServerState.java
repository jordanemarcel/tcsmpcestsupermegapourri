package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloServerState extends TCSMPState {

	private final static int TIMEOUT = 10000;
	
	private boolean send = false;
	private boolean error = false;
	private int timeout = 0;
	
	public TeloServerState() {
		super(TIMEOUT);
	}
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (isTimeout()) {
			
			if (getTimeoutState() == TIMEOUT_WRITE) {
				bb.clear();
				bb.put(TCSMPParser.encode("500 Connection timeouted\r\n"));
				bb.flip();
				setTimeoutState(TIMEOUT_CLOSE);
				return new Response(ResponseAction.WRITE);
			}
			
			if (getTimeoutState() == TIMEOUT_CLOSE) {
				return new Response(ResponseAction.CLOSE);
			}
		}
		
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

		/** 
		 * Change domain
		 */
		proto.setClientDomain(args[1]);
		
		/**
		 * Create response buffer.
		 */
		bb.put(TCSMPParser.encode("250-TCSMPv1\r\n250 OK greets " + args[1] + "\r\n"));
		bb.flip();
		
		return new Response(ResponseAction.WRITE);
	}
}