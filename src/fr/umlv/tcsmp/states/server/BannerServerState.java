package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class BannerServerState extends TCSMPState {

	private static String banner = "200 Hello I'm the TCSMP server. Nice to meet you.\r\n";
	private boolean send = false;
	
	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (send) {
			proto.setState(new TeloServerState());
			return new Response(ResponseAction.READ);
		}
		
		send = true;
		bb.clear();
		bb.put(TCSMPParser.encode(banner));
		bb.flip();
		return new Response(ResponseAction.REPLY); 
	}
}
