package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.states.TCSMPState;

public class Protocol {

	private TCSMPState state;
	
	private String from;
	private String domain;
	
	public Protocol(TCSMPState defaultState) {
		state = defaultState;
	}
	
	public Response doIt(ByteBuffer bb) {
		return state.processCommand(this, bb);
	}
	
	public void setState(TCSMPState state) {
		this.state = state;
	}
	
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getFrom() {
		return from;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getDomain() {
		return domain;
	}
}
