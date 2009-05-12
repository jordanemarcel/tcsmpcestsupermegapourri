package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ApzlServerState extends TCSMPState {

	private boolean send = false;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			proto.setState(new MailServerState());
			return new Response(ResponseAction.READ);
		}

		send = true;
		
		String [] args = TCSMPParser.parseCommand(bb);

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}
		
		if (args.length != 1 || args[0].equals("APZL") == false) {
			bb.clear();
			bb.put(ErrorReplies.unknowCommand("APZL", args[0]));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * Check on the domain.
		 * 		- create a puzzle if it's our domain
		 * 		- forward command if domain is not know
		 */
		if (proto.isRelay() == false) {
			Puzzle p = Puzzle.randomPuzzle(4, 4);
			bb.clear();
			bb.put(TCSMPParser.encode("215 " + proto.getMyDomains().get(0) + " 4,4 " + p.lineString() + "\r\n"));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}

		/* reset cmd */
		bb.position(0);
		
		/* gni */
		return new Response(ResponseAction.RELAYALL);
	}
}
