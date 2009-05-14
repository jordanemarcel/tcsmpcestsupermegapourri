package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.states.server.QuitServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloClientState extends TCSMPState {
	ResponseAction resp = null;
	ArrayList<String> list = new ArrayList<String>();

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			// Request has not yet been sent
			bb.put(TCSMPParser.encode("TELO "));
			// TODO the domain is the server's, not the client's
			bb.put(TCSMPParser.encode(proto.getClientDomain()));
			bb.put(TCSMPParser.encode("\r\n"));

			bb.flip();
			resp = ResponseAction.WRITE;

			return new Response(resp);
		}

		if (resp == ResponseAction.WRITE) {
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			list.clear();
			if (TCSMPParser.parseAnswer(bb, list)) {
				QuitClientState quiteState = null;

				for(int i=0; i<list.size(); i+=2) {
					switch(Integer.parseInt(list.get(i))) {
					// States
					case 250:
						proto.setState(new FromClientState());
						break;
					case 504:
					case 550:
					default:
						if (quiteState == null) {
							quiteState = new QuitClientState();
							proto.setState(quiteState);
						}
						proto.addMainError(list.get(i) + " " + list.get(i+1));
						break;
					}
				}
				bb.clear();
				return proto.doIt(bb);
			}
		}

		bb.clear();
		// Multiline didn't end, read next lines
		return new Response(ResponseAction.READ);
	}
}
