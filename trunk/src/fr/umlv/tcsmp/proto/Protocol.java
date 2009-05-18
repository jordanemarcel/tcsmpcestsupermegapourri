package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.umlv.tcsmp.handlers.PrintHandler;
import fr.umlv.tcsmp.handlers.TCSMPHandler;
import fr.umlv.tcsmp.mail.Message;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class Protocol {

	private final List<String> myDomains; 
	private TCSMPState state;
	private final ProtocolMode protocolMode;
	private int protocolPort = 2626;

	private String clientDomain;
	private String defaultRelay;

	private final Map<String, Puzzle> puzzles;
	private final Map<String, StringBuilder> domainErrors;
	private final StringBuilder mainErrors;

	private TCSMPHandler messageHandler;

	// TODO escape "." ?
	private final Message message;

	public Protocol(ProtocolMode mode, TCSMPHandler handler) {
		switch(mode) {
		case CLIENT:
			state = TCSMPState.newDefaultClientState();
			break;
		case SERVER:
			state = TCSMPState.newDefaultServerState();
			break;
		}
		messageHandler = handler;
		message = new Message();
		myDomains = new ArrayList<String>();
		puzzles = new HashMap<String, Puzzle>();
		protocolMode = mode;
		domainErrors = new HashMap<String, StringBuilder>();
		mainErrors = new StringBuilder();
	}

	public Protocol(ProtocolMode mode, int protocolPort) {
		this(mode);
		this.protocolPort = protocolPort;
	}

	public Protocol(ProtocolMode mode) {
		this(mode, new PrintHandler());
	}

	public int getProtocolPort() {
		return protocolPort;
	}

	public void setMessageHandler(TCSMPHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public List<String> getRecpts() {
		return Collections.unmodifiableList(message.getRcpts());
	}
	
	public void clearRecpts() {
		message.getRcpts().clear();
	}
	
	public void removeRcpt(String rcpt) {
		message.getRcpts().remove(rcpt);
	}

	public void addRcpt(String rcpt) {
		message.addRctp(rcpt);
	}
	
	public boolean containsRcpt(String rcpt) {
		return message.getRcpts().contains(rcpt.toLowerCase());
	}

	public ProtocolMode getProtocolMode() {
		return protocolMode;
	}

	public String getMainErrors() {
		return mainErrors.toString();
	}

	public void setDefaultRelay(String defaultRelay) {
		this.defaultRelay = defaultRelay;
	}
	
	public String getDefaultRelay() {
		return defaultRelay;
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
			for (String rcpt: message.getRcpts()) {
				System.out.println(rcpt);
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

	public void removePuzzleFor(String domain) {
		puzzles.remove(domain);
	}

	public Map<String, Puzzle> getPuzzles() {
		return puzzles;
	}

	public Message getMessage() {
		return message;
	}

	public Response doIt(ByteBuffer bb) {
		/* check for end of command */
		if (bb != null && bb.position() != 0) {
			if (TCSMPParser.decode(bb).endsWith("\n") == false) {
				/* pos and limit have not been altered. */
				return new Response(ResponseAction.CONTINUEREAD);
			}
		}

		/* exit state */
		return state.processCommand(this, bb);
	}

	/**
	 * Just relaying the isTimeout to the state.
	 */
	public boolean isTimeout() {
		return state.isTimeout();
	}

	/**
	 * Just realaying a cancel to the state
	 */
	public Response cancel(ByteBuffer bb) {
		return state.cancel(this, bb);
	}

	/**
	 * @return a new Protocol identical to this one. 
	 */
	public Protocol newProtocol() {
		return newProtocol(protocolMode);
	}

	public Protocol newProtocol(ProtocolMode mode) {
		Protocol pr = new Protocol(mode);
		pr.clientDomain = clientDomain;
		pr.messageHandler = messageHandler;
		pr.protocolPort = protocolPort;
		pr.message.copy(message);
		for (String d : myDomains) {
			pr.myDomains.add(d);
		}
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
		message.setFrom(from);
	}

	public String getFrom() {
		return message.getFrom();
	}

	public void setClientDomain(String domain) {
		this.clientDomain = domain.toLowerCase();
	}

	public String getClientDomain() {
		return clientDomain;
	}

	public List<String> getMyDomains() {
		return myDomains;
	}

	public void mail(String line) {
		message.data(line);
	}

	public String getMail() {
		return message.getLongMail();
	}

	public void processMessage() {
		if (messageHandler != null) {
			messageHandler.processMessage(message);
		}
	}
}
