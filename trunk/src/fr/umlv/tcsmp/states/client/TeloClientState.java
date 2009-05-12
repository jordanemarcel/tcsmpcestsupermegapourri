package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class TeloClientState extends TCSMPState {
	ResponseAction resp = null;

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			// Request has not yet been sent
			bb.clear();
			bb.put(TCSMPParser.encode("TELO "));
			// TODO the domain is the server's, not the client's
			bb.put(TCSMPParser.encode(proto.getClientDomain()));
			bb.put(TCSMPParser.encode("\r\n"));

			bb.flip();
			resp = ResponseAction.REPLY;

			return new Response(resp);
		}

		if (resp == ResponseAction.REPLY) {
			resp = ResponseAction.READ;
			return new Response(resp);
		}

		if (resp == ResponseAction.READ) {
			ArrayList<String> list = new ArrayList<String>();
			TCSMPParser.parseAnswer(bb, list);
			switch(Integer.parseInt(list.get(0))) {
			// States
			case 250:
				proto.setState(new FromClientState());
				bb.clear();
				return proto.doIt(bb);
			case 504:
				// TODO
				break;
			case 550:
				// TODO
				break;
			default:
				throw new AssertionError("Pouet");
			}
		}

		return null;
	}
}
