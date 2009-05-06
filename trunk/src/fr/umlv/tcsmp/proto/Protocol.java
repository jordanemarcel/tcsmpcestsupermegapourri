package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.states.TCSMPState;

public class Protocol {

	private TCSMPState state;
	
	public Protocol(TCSMPState defaultState) {
		state = defaultState;
	}
	
	public Response doIt(ByteBuffer bb) {
		return state.processCommand(this, bb);
	}
	
	public void setState(TCSMPState state) {
		this.state = state;
	}
}
