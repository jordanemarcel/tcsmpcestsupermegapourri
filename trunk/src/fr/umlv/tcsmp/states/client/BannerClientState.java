package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class BannerClientState extends TCSMPState {

	private final List<String> commandArgs;
	
	public BannerClientState() {
		commandArgs = new ArrayList<String>();
	}

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (TCSMPParser.parseAnswer(bb, commandArgs)) {
			// Multiline ended
			// TODO: check response codes
			proto.setState(new TeloClientState());
			return proto.doIt(bb);
		}
		
		return new Response(ResponseAction.READ);
	}
}
