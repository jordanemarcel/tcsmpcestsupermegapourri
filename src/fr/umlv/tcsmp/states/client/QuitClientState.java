package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class QuitClientState extends TCSMPState {
	ResponseAction resp = null;

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			// Request has not yet been sent
			bb.put(TCSMPParser.encode("QUIT\r\n"));
			bb.flip();
			resp = ResponseAction.WRITE;

			return new Response(resp);
		}

		if (resp == ResponseAction.WRITE) {
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			ArrayList<String> list = new ArrayList<String>();
			TCSMPParser.parseAnswer(bb, list);

			switch(Integer.parseInt(list.get(0))) {
			// States
			case 221:
				break;
			default:
				throw new AssertionError("Pouet");
			}
			proto.setState(null);
			bb.clear();
			return new Response(ResponseAction.CLOSE);
		}

		return null;
	}	
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		bb.clear();
		if (resp == ResponseAction.WRITE) {
			proto.addMainError("Communication error while QUIT'ing.");
		}
		else {
			proto.addMainError("Communication error while getting QUIT response.");
		}
		return new Response(ResponseAction.CLOSE);
	}
}
