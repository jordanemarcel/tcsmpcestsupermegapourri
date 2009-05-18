package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.states.server.QuitServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class RctpClientState extends TCSMPState {

	ResponseAction resp = null;
	int index = 0;

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			bb.clear();
			// Request has not yet been sent
			bb.put(TCSMPParser.encode("RCPT <"));
			// TODO the domain is the server's, not the client's
			bb.put(TCSMPParser.encode(proto.getRecpts().get(index)));
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
				index++;
				break;
			case 450:
			case 451:
			case 452:
			case 503:
			case 550:
			case 551:
			case 552:
			case 553:
			default://TODO check? + record error somewhere maybe
				proto.addErrorFor("RCPT", proto.getRecpts().get(index), list.get(0) + " " + list.get(1));
				index++;
				// Don't remove, use error to known if something went wrong
				// proto.getRecpts().remove(index);
				break;
			}
			
			if (index >= proto.getRecpts().size()) {
				if (proto.getDomainErrors().size() == index) {
					proto.setState(new QuitClientState());
				}
				else {
					proto.setState(new ApzlClientState());
				}
			}
			else {
				resp = null;
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
			proto.addMainError("RCPT", "Communication error while RCPT'ing.");
		}
		else {
			proto.addMainError("RCPT", "Communication error while getting RCPT response.");
		}
		return new Response(ResponseAction.CLOSE);
	}
}
