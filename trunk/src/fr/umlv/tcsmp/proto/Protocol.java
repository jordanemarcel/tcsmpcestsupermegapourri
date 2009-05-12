package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import fr.umlv.tcsmp.states.TCSMPState;

public class Protocol {

	private TCSMPState state;
	private ProtocolMode protocolMode;
	private int protocolPort = 26;

	private String from;
	private String domain;

	private final StringBuilder mail;

	public Protocol(TCSMPState defaultState) {
		state = defaultState;
		mail = new StringBuilder();
	}

	public Protocol(TCSMPState defaultState, int protocolPort) {
		this(defaultState);
		this.protocolPort = protocolPort;
	}

	public int getProtocolPort() {
		return protocolPort;
	}

	public void setProtocolMode(ProtocolMode protocolMode) {
		this.protocolMode = protocolMode;
	}

	public ProtocolMode getProtocolMode() {
		return protocolMode;
	}

	public Response doIt(ByteBuffer bb) {
		/* check for end of command */
		if (bb != null) {
			if (new String(bb.array()).endsWith("\n") == false) {
				return new Response(ResponseAction.CONTINUEREAD);
			}
		}

		/* exit state */
		return state.processCommand(this, bb);
	}

	public Protocol newProtocol() {
		Protocol pr;
		switch (protocolMode) {
		case CLIENT:
			pr = new Protocol(TCSMPState.newDefaultClientState());
			break;
		case SERVER:
			pr = new Protocol(TCSMPState.newDefaultServerState());
			break;
		default:
			return null;
		}
		pr.domain = domain;
		pr.from = from;
		pr.mail.append(mail);
		return pr;
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

	public void mail(String line) {
		mail.append(line);
	}
}
