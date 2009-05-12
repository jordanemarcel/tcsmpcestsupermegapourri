package fr.umlv.tcsmp.states;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.server.BannerServerState;

/**
 * Class representing a state in the TCSMP proto. 
 */
public abstract class TCSMPState {
	
	/**
	 * Parse TCSMP command found in the ByteBuffer and
	 * return a Response englobbing several useful things
	 * for the proto. handler.
	 */
	public abstract Response processCommand(Protocol proto, ByteBuffer bb);
	
	/**
	 * Return a new default state for a server.
	 */
	public static TCSMPState newDefaultServerState() {
		return new BannerServerState();
	}

	/**
	 * Return a new default state for a client 
	 */
	public static TCSMPState newDefaultClientState() {
		return new BannerClientState();
	}
}
