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

	private boolean send = false;
	
	private Protocol fakeProto;
	private String currentDomain;
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {

		/** we are in a relaying mode
		 */
		if (fakeProto != null) {
			
			Response res = fakeProto.doIt(bb);
			
			if (res.getAction() == ResponseAction.READ && send == false) {
				fakeProto = null;
				bb.position(0);
				return new Response(ResponseAction.REPLY);
			}
			
			/* last state, we have to tell to reply the response to the client */
			if (fakeProto.getState().getClass().equals(PkeyClientState.class)) {
				send = false;
			}
			
			if (res.getAction() != ResponseAction.READ)
				return new Response(currentDomain, ResponseAction.RELAY);
			return res;
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
			return new Response(ResponseAction.REPLY);
		}

		/**
		 * check to see if it's a solution for us or not
		 * XXX: foo
		 */
		if (proto.isRelay(args[1]) == false) {
			/*
			 * XXX: We are asuming that puzzle solution is right 
			 */
			bb.put(TCSMPParser.encode("216 Your mail has been kept !\r\n"));
			bb.flip();
			return new Response(ResponseAction.REPLY);
		}
		
		/**
		 * XXX: check if domain is a valid one
		 */
		
		
		
		/**
		 * We have to forward the MAIL to the server before
		 * sending the PKEY command.
		 */
		
		/**
		 * Add the puzzle in the proto
		 */
		String dims = args[2];
		String desc = args[3];
		Puzzle puzzle = TCSMPParser.parsePuzzleDesc(dims, desc);
		proto.addPuzzleFor(args[1], puzzle);
		
		/**
		 * Create a fakeProto
		 */
		fakeProto = proto.newProtocol();
		fakeProto.setState(new MailClientState());
		send = true;
		currentDomain = args[1];
		
		Response res = fakeProto.doIt(bb);
		if (res.getAction() != ResponseAction.READ)
			return new Response(currentDomain, ResponseAction.RELAY);
		return res;
	}

}
