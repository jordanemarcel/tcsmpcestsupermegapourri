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
	private boolean error = false;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		if (send) {
			if (error == false)
				proto.setState(new MailServerState());
			else
				send = error = false;
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
			return new Response(ResponseAction.WRITE);
		}
		
		// I'm concerned.
		if (proto.isRelay() == false) {
			// XXX: puzzle size must be dynamic
			Puzzle p = Puzzle.randomPuzzle(4, 4);
			proto.addPuzzleFor(proto.getClientDomain(), p);
			bb.clear();
			bb.put(TCSMPParser.encode("215 " + proto.getMyDomains().get(0) + "4,4 " + p.lineString() + "\r\n"));
			bb.flip();
			return new Response(ResponseAction.WRITE);
		}

		// reset cmd
		bb.position(0);

		// send it to any RCPT
		return new Response(ResponseAction.RELAYALL);
	}
}
