package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.TCSMPCommandParser;
import fr.umlv.tcsmp.states.TCSMPState;

public class TeloState implements TCSMPState {

	public Response processCommand(ByteBuffer bb) {
		
		String [] args = TCSMPCommandParser.parse(bb);
		
		if (args.length == 0 || args[0].equals("TELO") == false) {
			System.out.println("fook");
			return null;
		}
		
		System.out.println("ok");
		return new Response();
	}
}
