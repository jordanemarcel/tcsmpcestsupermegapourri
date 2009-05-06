package fr.umlv.tcsmp.states;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;

/**
 * Class representing a state in the TCSMP proto. 
 */
public interface TCSMPState {
	
	/**
	 * Parse TCSMP command found in the ByteBuffer and
	 * return a Response englobbing several useful things
	 * for the proto. handler.
	 */
	public Response processCommand(Protocol proto, ByteBuffer bb);
}
