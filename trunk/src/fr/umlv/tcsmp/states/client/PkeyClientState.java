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

	private void checkIfFinished(Protocol proto) {
		if (processedDomains.size() == proto.getPuzzles().size()) {
			// Done processing puzzles
			proto.setState(new QuitClientState());
		}
		else {
			proto.setState(this);
		}
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
				Puzzle.resolve(puzzle);
				currentDomain = entry.getKey();
				break;
			}

			String s =  "PKEY " + currentDomain + " " + puzzle.getWidth() + "," + puzzle.getHeight() +
				" " + puzzle.lineString() + "\r\n";
			bb.put(TCSMPParser.encode(s));

			bb.flip();
			resp = ResponseAction.WRITE;

			return new Response(resp);
		}

		if (resp == ResponseAction.WRITE) {
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
			case 216:
				processedDomains.put(currentDomain, true);
				checkIfFinished(proto);
				break;
			case 517:
				proto.setState(new MailClientState(this));
				break;
			case 516:
			default:
				proto.addErrorFor(currentDomain, list.get(0) + " " + list.get(1));
				processedDomains.put(currentDomain, true);
				checkIfFinished(proto);
				break;
			}
			
			resp = null;
			
			bb.clear();
			return proto.doIt(bb);
		}

		return null;
	}

}
