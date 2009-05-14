package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.states.client.MailClientState;
import fr.umlv.tcsmp.states.client.PkeyClientState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class PkeyServerState extends TCSMPState {
	
	private final static int TIMEOUT = 600000; // 10 minutes
	
	private boolean send = false;
	private boolean error = false;
	private int pkeyTry = 0;

	private Protocol fakeProto;
	private String currentDomain;

	public PkeyServerState() {
		super(TIMEOUT);
	}
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {

		// we are in a relaying mode
		if (fakeProto != null) {

			String serverResponse = TCSMPParser.decode(bb);
			
			Response res = fakeProto.doIt(bb);

			// We can reply the code to the client
			if (send == false) {
				if (res.getAction() == ResponseAction.WRITE) {
					fakeProto = null;
					// XXX: check the response to see if soluce is good or not
					// in order to see if we can switch state
					proto.setState(new QuitServerState());
					bb.clear();
					bb.put(TCSMPParser.encode(serverResponse));
					bb.flip();
					return new Response(ResponseAction.WRITE);
				}
				else {
					return new Response(currentDomain, ResponseAction.READ);
				}
			}

			// last state, we have to tell to reply the response to the client
			if (fakeProto.getState().getClass().equals(PkeyClientState.class)) {
				send = false;
			}

			if (res.getAction() != ResponseAction.READ)
				return new Response(currentDomain, ResponseAction.WRITE);

			return new Response(currentDomain, ResponseAction.READ);
		}
		
		// are we in timeout waiting for RCTP
		if (isTimeout())
			return timeoutResponse(bb);
		else
			timeoutReset();
		
		// send OK ?
		if (send) {
			// state was in error, just need need READ
			if (error == false) {
				proto.setState(new QuitServerState());
			} else {	
				error = false;
				send = false;
			}
			
			bb.clear();
			return new Response(ResponseAction.READ);
		}

		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();

		if (args.length == 1 && args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}

		if (args.length != 4 || args[0].equals("PKEY") == false) {
			bb.put(ErrorReplies.unknowCommand("PKEY", args[0]));
			bb.flip();
			error = true;
			return new Response(ResponseAction.WRITE);
		}


		/**
		 * Add the puzzle in the proto
		 */
		String dims = args[2];
		String desc = args[3];
		Puzzle puzzle = TCSMPParser.parsePuzzleDesc(dims, desc);

		/**
		 * check to see if it's a solution for us or not
		 * XXX: foo
		 */
		if (proto.isRelay(args[1]) == false) {
			Puzzle p = proto.getPuzzleFor(proto.getClientDomain());
			if (puzzle.equals(p) && Puzzle.isResolved(puzzle)) {
				bb.put(TCSMPParser.encode("216 Your mail has been kept !\r\n"));
			}
			else {
				pkeyTry++;
				bb.put(TCSMPParser.encode("516 Ahah dude... you FAIL.\r\n"));
				error = true;
			}
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}
		else
		{
			// we have to add the PKEY in the puzzles for the PkeyClient.
			proto.addPuzzleFor(args[1], puzzle);
		}


		// fake proto for client state
		fakeProto = proto.newProtocol();
		fakeProto.setState(new MailClientState());
		send = true;
		currentDomain = args[1];
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ)
			return new Response(currentDomain, ResponseAction.WRITE);
		
		return new Response(currentDomain, ResponseAction.READ);
	}

}
