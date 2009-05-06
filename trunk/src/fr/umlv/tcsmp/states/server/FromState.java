package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;

public class FromState implements TCSMPState {

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		return null;
	}
}
