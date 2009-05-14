package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class Protocol {

	private final List<String> myDomains; 
	private TCSMPState state;
	private final ProtocolMode protocolMode;
	private int protocolPort = 26;

	private final List<String> rcpts;
	private String from;
	private String clientDomain;

	private final Map<String, Puzzle> puzzles;
	private final Map<String, StringBuilder> domainErrors;
	private final StringBuilder mainErrors;
	
	// TODO escape "." ?
	private final StringBuilder mail;

	public Protocol(ProtocolMode mode) {
		switch(mode) {
		case CLIENT:
			state = TCSMPState.newDefaultClientState();
			break;
		case SERVER:
			state = TCSMPState.newDefaultServerState();
			break;
		}
		mail = new StringBuilder();
		myDomains = new ArrayList<String>();
		rcpts = new ArrayList<String>();
		puzzles = new HashMap<String, Puzzle>();
		protocolMode = mode;
		domainErrors = new HashMap<String, StringBuilder>();
		mainErrors = new StringBuilder();
	}

	public Protocol(ProtocolMode mode, int protocolPort) {
		this(mode);
		this.protocolPort = protocolPort;
	}

	public int getProtocolPort() {
		return protocolPort;
	}

	public List<String> getRecpts() {
		return rcpts;
	}

	public ProtocolMode getProtocolMode() {
		return protocolMode;
	}
	
	public String getMainErrors() {
		return mainErrors.toString();
	}
	
	public void addMainError(String error) {
		if (mainErrors.length() != 0) {
			mainErrors.append("\n");
		}
		mainErrors.append(error);
	}
	
	public Map<String, StringBuilder> getDomainErrors() {
		return domainErrors;
	}
	
	public void addErrorFor(String domain, String errorString) {
		StringBuilder errorBuilder;
		errorBuilder = domainErrors.get(domain);
		if (errorBuilder == null) {
			errorBuilder = new StringBuilder();
			domainErrors.put(domain, errorBuilder);
		}
		errorBuilder.append(errorString);
	}

	public TCSMPState getState() {
		return state;
	}

	public void addDomain(String domain) {
		myDomains.add(domain);
	}

	public boolean isRelay(String domain) {
		return myDomains.contains(domain) == false;
	}

	public boolean isRelay() {
		try {
			for (String rcpt: rcpts) {
				if (isRelay(TCSMPParser.parseDomain(rcpt)))
					return true;
			}
		} catch (ParseException e) {
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

	public Map<String, Puzzle> getPuzzles() {
		return puzzles;
	}

	public StringBuilder getMail() {
		return mail;
	}

	public Response doIt(ByteBuffer bb) {
		/* check for end of command */
		if (bb != null && bb.position() != 0) {
			if (TCSMPParser.decode(bb).endsWith("\n") == false) {
				/* pos and limit have not been altered. */
				return new Response(ResponseAction.CONTINUEREAD);
			}
			if (TCSMPParser.decode(bb).startsWith("\n") == true ||
					TCSMPParser.decode(bb).startsWith("\r") == true) {
				/* blank line, ignore it. */
				bb.clear();
				return new Response(ResponseAction.READ);
			}
		}

		/* exit state */
		return state.processCommand(this, bb);
	}

	public Protocol newProtocol() {
		Protocol pr = new Protocol(protocolMode);
		pr.clientDomain = clientDomain;
		pr.from = from;
		pr.mail.append(mail);
		for (String d : myDomains)
			pr.myDomains.add(d);
		for (String r : rcpts)
			pr.rcpts.add(r);
		for (Entry<String, Puzzle> p : puzzles.entrySet()) {
			pr.puzzles.put(p.getKey(), p.getValue());
		}
		/* XXX: ... */
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
