package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.LinkedList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class QuitServerState extends TCSMPState {
	
	private final static int TIMEOUT = 60000; // 1 minutes
	
	private boolean send = false;
	
	// domains not treated
	private LinkedList<String> domains;
	
	public QuitServerState() {
		super(TIMEOUT);
	}
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		// are we in timeout
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		// we are in a relaying mode
		if (domains != null) {
			
			// finished reply the last QUIT cmd to the client
			if (domains.size() == 0) {
				bb.position(0);
				send = true;
				domains = null;
				return new Response(ResponseAction.WRITE);
			}
			
			// read the next domain
			return new Response(domains.removeFirst(), ResponseAction.READ);
		}
		
		if (send) {
			bb.clear();
			return new Response(ResponseAction.CLOSE);
		}
		
		send = true;
		String [] args = TCSMPParser.parseCommand(bb);
		
		if (args.length == 0) {
			bb.clear();
			bb.put(ErrorReplies.syntaxError());
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		if (args[0].equals("QUIT") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("QUIT", args[0]));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		// Check if we have to forward the command.
		if (proto.isRelay() == false) {
			bb.clear();
			bb.put(TCSMPParser.encode("221 See you next time~~~~!\r\n"));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}
		
		// Reset command and forward
		bb.position(0);
		
		// create a list with all the domain of the rctp
		domains = new LinkedList<String>();
		for (String r : proto.getRecpts()) {
			try {
				String domain = TCSMPParser.parseDomain(r); 
				if (!domains.contains(domain))
					domains.add(domain);
			} catch (ParseException e) {
			}
		}

		return new Response(ResponseAction.RELAYALL);
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		// relaying state ? ignore the error and go to the next domain
		if (domains != null) {
			// read the next domain
			return new Response(domains.removeFirst(), ResponseAction.READ);
		}

		// unknow error
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}
}
