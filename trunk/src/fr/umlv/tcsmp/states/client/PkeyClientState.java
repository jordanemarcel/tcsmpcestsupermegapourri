package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class PkeyClientState extends TCSMPState {

	private ResponseAction resp = null;
	private Map<String, Boolean> processedDomains = null;
	private ArrayList<String> list = new ArrayList<String>();

	private String currentDomain;

	public PkeyClientState() {
		processedDomains = new HashMap<String, Boolean>();
	}

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			Puzzle puzzle = null;
			// All puzzle solutions have not yet been sent
			for(Entry<String, Puzzle> entry : proto.getPuzzles().entrySet()) {
				if (processedDomains.get(entry.getKey()) != null) {
					continue;
				}
				if (entry.getValue() == null) {
					// If for some reason we got no puzzle, consider it processed
					processedDomains.put(entry.getKey(), true);
				}
				puzzle = entry.getValue();
					// XXX Booya
				puzzle.resolve();
				currentDomain = entry.getKey();
				break;
			}

			String s =  "PKEY " + currentDomain + " " + puzzle.getWidth() + "," + puzzle.getHeight() +
				" " + puzzle.lineString() + "\r\n";
			bb.put(TCSMPParser.encode(s));

			bb.flip();
			resp = ResponseAction.REPLY;

			return new Response(resp);
		}

		if (resp == ResponseAction.REPLY) {
			// Request was sent, signify we want to get the reply
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			// We got here because we got the answer
			list.clear();
			TCSMPParser.parseAnswer(bb, list);

			switch(Integer.parseInt(list.get(0))) {
			// States
			// TODO add check if MAIL was not found on server
			case 216:
				resp = null;
				processedDomains.put(currentDomain, true);
				if (processedDomains.size() == proto.getPuzzles().size()) {
					// Done processing puzzles
					proto.setState(new QuitClientState());
				}
				else {
					proto.setState(this);
				}
				bb.clear();
				return proto.doIt(bb);
			default:
				throw new AssertionError("Pouet");
			}
		}

		return null;
	}

}
