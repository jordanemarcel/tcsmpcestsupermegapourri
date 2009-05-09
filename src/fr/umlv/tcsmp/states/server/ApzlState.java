package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ApzlState implements TCSMPState {

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		String [] args = TCSMPParser.parse(bb);

		if (args.length != 1 || args[0].equals("APZL") == false) {
			return new Response(ErrorReplies.unknowCommand("APZL", args[0]));
		}

		/**
		 * Change state
		 */
		proto.setState(new MailState());
		
		/**
		 * Check on the domain.
		 * 		- create a puzzle if it's our domain
		 * 		- forward command if domain is not know
		 */
		/* XXX */
		if (proto.getDomain().equals("clem1.be")) {
			Puzzle p = Puzzle.randomPuzzle(2, 2);
			ByteBuffer response = ByteBuffer.wrap(new String("215 " + proto.getDomain() + " 4,4 " + p.lineString() + "\r\n").getBytes());
			return new Response(response);
		}

		/* Forward command */
		
		return new Response(bb, ResponseAction.RELAYALL);
	}
}
