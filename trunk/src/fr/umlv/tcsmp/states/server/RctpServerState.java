package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class RctpServerState extends TCSMPState {

	private boolean send = false;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			send = false;
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
		
		if (args.length > 2 || (args[0].equals("RCPT") == false && args[0].equals("APZL") == false)) {
			bb.put(ErrorReplies.unknowCommand("RCPT|APZL", args[0]));
			return new Response(ResponseAction.REPLY);
		}
		
		if (args[0].equals("APZL")) {
			/* bypass normal proccessings */
			TCSMPState apzlState = new ApzlServerState();
			proto.setState(apzlState);
			return apzlState.processCommand(proto, bb);
		}
		
		String dest = null;
		try {
			dest = TCSMPParser.parseDomain(args[1]);
		} catch (ParseException e) {
			bb.put(TCSMPParser.encode("501 Not a valid address."));
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * XXX: Here we have to see if we have to forward
		 * command or if we catch it.
		 * On va faire des genres de sous state pour helo et tout ca... 
		 */
		
		if (proto.isRelay(dest) == false) {
			/** XXX: see if user exists here ? */
			bb.put(TCSMPParser.encode("250 OK"));
			return new Response(ResponseAction.REPLY);
		}
		
		/* bb will be forwarded to the dest domain */
		return new Response(dest, ResponseAction.RELAY);
	}
}
