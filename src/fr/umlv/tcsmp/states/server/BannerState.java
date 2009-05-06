package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;

public class BannerState implements TCSMPState {

	private static String banner = "200 Hello I'm the TCSMP server. Nice to meet you.";

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		proto.setState(new TeloState());
		return new Response(ByteBuffer.wrap(banner.getBytes())); 
	}
}
