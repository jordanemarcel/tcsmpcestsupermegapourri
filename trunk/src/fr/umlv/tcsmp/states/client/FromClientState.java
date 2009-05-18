package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class FromClientState extends TCSMPState {
	ResponseAction resp = null;

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			bb.clear();
			// Request has not yet been sent
			bb.put(TCSMPParser.encode("FROM <"));
			// TODO the domain is the server's, not the client's
			bb.put(TCSMPParser.encode(proto.getFrom()));
			bb.put(TCSMPParser.encode(">\r\n"));

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
			ArrayList<String> list = new ArrayList<String>();
			TCSMPParser.parseAnswer(bb, list);
			switch(Integer.parseInt(list.get(0))) {
			// States
			case 250:
				proto.setState(new RctpClientState());
				break;
			default:
				proto.setState(new QuitClientState());
				proto.addMainError(list.get(0) + " " + list.get(1));
				break;
			}

			bb.clear();
			return proto.doIt(bb);
		}

		return null;
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		bb.clear();
		if (resp == ResponseAction.WRITE) {
			proto.addMainError("Communication error while FROM'ing.");
		}
		else {
			proto.addMainError("Communication error while getting FROM response.");
		}
		return new Response(ResponseAction.CLOSE);
	}
}
