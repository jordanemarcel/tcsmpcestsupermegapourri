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
	private ResponseAction resp = null;

	public BannerClientState() {
		commandArgs = new ArrayList<String>();
	}

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (resp == null) {
			resp = ResponseAction.READ;
			return new Response(resp);
		}
		
		if (TCSMPParser.parseAnswer(bb, commandArgs)) {
			// Multiline ended

			QuitClientState quiteState = null;

			for(int i=0; i<commandArgs.size(); i+=2) {
				switch (Integer.parseInt(commandArgs.get(i))) {
				case 220:
					proto.setState(new TeloClientState());
					break;
				case 554:
				default:
					if (quiteState == null) {
						quiteState = new QuitClientState();
						proto.setState(quiteState);
					}
					proto.addMainError("Banner", commandArgs.get(i) + " " + commandArgs.get(i+1));
					break;
				}
			}
			bb.clear();
			return proto.doIt(bb);
		}
		bb.clear();
		// Multiline didn't end, read next lines
		return new Response(ResponseAction.READ);
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		bb.clear();
		proto.addMainError("Banner", "Communication error while getting the banner.");
		return new Response(ResponseAction.CLOSE);
	}
}
