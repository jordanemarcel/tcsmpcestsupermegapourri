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
			// Request has not yet been sent
			bb.clear();
			bb.put(TCSMPParser.encode("FROM <"));
			// TODO the domain is the server's, not the client's
			bb.put(TCSMPParser.encode(proto.getFrom()));
			bb.put(TCSMPParser.encode("FROM >"));

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
			ArrayList<String> list = new ArrayList<String>();
			TCSMPParser.parseAnswer(bb, list);
			switch(Integer.parseInt(list.get(0))) {
			// States
			case 200:
				proto.setState(new RctpClientState());
				return proto.doIt(bb);
			default:
				throw new AssertionError("Pouet");
			}
		}
		
		return null;
	}
}
