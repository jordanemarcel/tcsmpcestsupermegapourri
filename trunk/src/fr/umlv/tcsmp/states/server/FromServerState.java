package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class FromServerState extends TCSMPState {

	private final static int TIMEOUT = 300000; // 5 minutes
	
	private boolean send = false;
	private boolean error = false;

	public FromServerState() {
		super(TIMEOUT);
	}
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		// send OK ?
		if (send) {
			if (error == false)
				proto.setState(new RctpServerState());
			else
				send = error = false;
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

		if (args.length != 2 || args[0].equals("FROM") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("FROM", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		/** 
		 * Set from
		 */
		try {
			String domain = TCSMPParser.parseDomain(args[1]);
			String user = TCSMPParser.parseUser(args[1]);
			if (domain.equals(proto.getClientDomain()) == false) {
				error = true;
				bb.put(TCSMPParser.encode(new String("555 Open relaying is disabled.\r\n")));
				bb.flip();
				return new Response(ResponseAction.WRITE);
			}
			proto.setFrom(user + "@" + domain);
		} catch (ParseException e) {
			error = true;
			bb.put(TCSMPParser.encode(new String("500 Invalid from.\r\n")));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		/**
		 * Create response buffer.
		 */
		bb.put(TCSMPParser.encode(new String("250 OK\r\n")));
		bb.flip();

		return new Response(ResponseAction.WRITE);
	}
}
