package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ApzlClientState extends TCSMPState {
	private ResponseAction resp = null;
	private ArrayList<String> list = new ArrayList<String>();

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			bb.clear();
			// Request has not yet been sent
			bb.put(TCSMPParser.encode("APZL\r\n"));

			bb.flip();
			resp = ResponseAction.WRITE;

			return new Response(resp);
		}

		if (resp == ResponseAction.WRITE) {
			// Request was sent, signify we want to get the replies
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			// We got here because we got the answer
			
			if (TCSMPParser.parseAnswer(bb, list)) {
				// Multiline ended
				for(int i=0; i<list.size(); i+=2) {
					switch(Integer.parseInt(list.get(i))) {
					// States
					case 215:
						String[] strings = list.get(i+1).split(" ");
						String domain = strings[0];
						String dims = strings[1];
						String desc = strings[2];
						Puzzle puzzle = TCSMPParser.parsePuzzleDesc(dims, desc);
						proto.addPuzzleFor(domain, puzzle);
						break;
					case 515:
					default:
						// NOOP TODO maybe record error msg?
						proto.addErrorFor("APZL", "Unknown" + i/2, list.get(i) + " " + list.get(i+1));
						break;
					}
				}
				
				proto.setState(new QuitClientState());	
				for(Map.Entry<String, Puzzle> entry :proto.getPuzzles().entrySet()) {
					if (entry.getValue() != null) {
						proto.setState(new MailClientState());	
					}
				}
					
				resp = null;
				bb.clear();
				return proto.doIt(bb);
			}

			bb.clear();
			// Multiline didn't end, read next lines
			return new Response(ResponseAction.READ);

		}

		return null;
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		bb.clear();
		if (resp == ResponseAction.WRITE) {
			proto.addMainError("APZL", "Communication error while APZL'ing.");
		}
		else {
			proto.addMainError("APZL", "Communication error while getting APZL response(s).");
		}
		return new Response(ResponseAction.CLOSE);
	}
}
