package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

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
			// Request has not yet been sent
			bb.clear();
			bb.put(TCSMPParser.encode("APZL\r\n"));

			bb.flip();
			resp = ResponseAction.REPLY;

			return new Response(resp);
		}

		if (resp == ResponseAction.REPLY) {
			// Request was sent, signify we want to get the replies
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			// We got here because we got the answer
			list.clear();

			boolean parseResult = TCSMPParser.parseAnswer(bb, list);

			for(int i=0; i<list.size(); i+=4) {
				switch(Integer.parseInt(list.get(i))) {
				// States
				case 200:
					String domain = list.get(i+1);
					String dims = list.get(i+2);
					String desc = list.get(i+3);
					Puzzle puzzle = TCSMPParser.parsePuzzleDesc(dims, desc);
					proto.addPuzzleFor(domain, puzzle);
					break;
				default:
					throw new AssertionError("Pouet");
				}
			}

			if (parseResult) {
				proto.setState(new MailClientState());		
				resp = null;
				return proto.doIt(bb);
			}


		}

		return null;
	}
}
