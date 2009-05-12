package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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

			for(int i=0; i<commandArgs.size(); i+=2) {
				switch (Integer.parseInt(commandArgs.get(i))) {
				case 220:
					// NOOP
					break;
				case 554:
					// TODO QUIT correctement.
					break;
				default:
					throw new AssertionError("I don't know about this response code: " + commandArgs.get(i) + " for connection establishment.");
				}
			}
			proto.setState(new TeloClientState());
			bb.clear();
			return proto.doIt(bb);
		}

		return new Response(ResponseAction.READ);
	}
}
