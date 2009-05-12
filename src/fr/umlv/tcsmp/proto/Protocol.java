package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;

public class Protocol {

	private final List<String> myDomains; 
	private TCSMPState state;
	private ProtocolMode protocolMode;
	private int protocolPort = 26;

	private final List<String> rcpts;
	private String from;
	private String clientDomain;
	
	private Map<String, Puzzle> puzzles;

	private final StringBuilder mail;

	public Protocol(TCSMPState defaultState) {
		state = defaultState;
		mail = new StringBuilder();
		myDomains = new ArrayList<String>();
		rcpts = new ArrayList<String>();
		puzzles = new HashMap<String, Puzzle>();
	}
	
	public Protocol(TCSMPState defaultState, int protocolPort) {
		this(defaultState);
		this.protocolPort = protocolPort;
	}

	public int getProtocolPort() {
		return protocolPort;
	}
	
	public List<String> getRecpts() {
		return rcpts;
	}

	public void setProtocolMode(ProtocolMode protocolMode) {
		this.protocolMode = protocolMode;
	}

	public ProtocolMode getProtocolMode() {
		return protocolMode;
	}
	
	
	public void addDomain(String domain) {
		myDomains.add(domain);
	}
	
	public boolean isRelay(String domain) {
		return myDomains.contains(domain) == false;
	}
	
	public boolean isRelay() {
		for (String rcpt: rcpts) {
			if (isRelay(rcpt))
				return true;
		}
		return false;
	}
	
	public Puzzle getPuzzleFor(String domain) {
		Puzzle puzzle = puzzles.get(domain);
		if (puzzle == null) {
			throw new AssertionError("Unknown domain.");
		}
		
		return puzzle;
	}
	
	public void addPuzzleFor(String domain, Puzzle puzzle) {
		if (puzzles.put(domain, puzzle) != null) {
			throw new AssertionError("Already registered a puzzle for the given domain.");
		}
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
		
		pr.clientDomain = clientDomain;
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

	public void setClientDomain(String domain) {
		this.clientDomain = domain;
	}

	public String getClientDomain() {
		return clientDomain;
	}
	
	public List<String> getMyDomains() {
		return myDomains;
	}

	public void mail(String line) {
		mail.append(line);
	}
}
