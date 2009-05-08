package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;

public class DataState implements TCSMPState {

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		/*
		 * A fuking state \o/
		 * Append data in string builder inside the proto ?
		 */
		/*
		if (bb.remaining() > 1024)
			foo
		*/
		
		return null;
	}
}
