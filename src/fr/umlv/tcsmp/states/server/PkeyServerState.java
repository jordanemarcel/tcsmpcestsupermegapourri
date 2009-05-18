package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
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
	private boolean pzlok = false;
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
					send = true;
					if (serverResponse.split(" ")[0].startsWith("2"))
						pzlok = true;
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
//			if (error == false && pzlok == true) {
//				//proto.setState(new QuitServerState());
//			} else {
				if (currentDomain != null)
					proto.removePuzzleFor(currentDomain);
				error = false;
				send = false;
//			}

			bb.clear();
			return new Response(ResponseAction.READ);
		}

		String [] args = TCSMPParser.parseCommand(bb);
		bb.clear();

		if (args.length == 0) {
			bb.clear();
			bb.put(ErrorReplies.syntaxError());
			bb.flip();
			send = true;
			error = true;
			return new Response(ResponseAction.WRITE);
		}

		if (args[0].equals("QUIT")) {
			TCSMPState t = new QuitServerState();
			proto.setState(t);
			return t.processCommand(proto, bb);
		}

		if (args.length != 4 || args[0].equals("PKEY") == false) {
			bb.put(ErrorReplies.unknowCommand("PKEY", args[0]));
			bb.flip();
			send = true;
			error = true;
			return new Response(ResponseAction.WRITE);
		}


		// create the matrice
		String dims = args[2];
		String desc = args[3];
		try {
			Puzzle puzzle = TCSMPParser.parsePuzzleDesc(dims, desc);
			// check solution
			if (proto.isRelay(args[1]) == false) {
				Puzzle p = proto.getPuzzleFor(proto.getClientDomain());
				if (puzzle.equals(p) && Puzzle.isSolved(puzzle)) {
					proto.processMessage();
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
		}
		catch (Exception e) {
			pkeyTry++;
			bb.put(TCSMPParser.encode("516 Ahah dude... bad puzzle... you FAIL.\r\n"));
			error = true;
			bb.flip();
			send = true;
			return new Response(ResponseAction.WRITE);
		}

		// fake proto for client state
		fakeProto = proto.newProtocol(ProtocolMode.CLIENT);
		fakeProto.setState(new MailClientState());
		send = true;
		currentDomain = args[1];
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ) {
			return new Response(currentDomain, ResponseAction.WRITE);
		}

		return new Response(currentDomain, ResponseAction.READ);
	}


	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		// relaying mode ? remove pkey and stop relaying
		if (fakeProto != null) {
			fakeProto = null;
			proto.removePuzzleFor(currentDomain);
		}

		// unexpected error occured
		bb.clear();
		bb.put(ErrorReplies.unexpectedError());
		bb.flip();
		return new Response(ResponseAction.WRITE);
	}

}
