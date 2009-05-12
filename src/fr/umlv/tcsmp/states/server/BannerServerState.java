package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;

public class BannerServerState extends TCSMPState {

	private static String banner = "200 Hello I'm the TCSMP server. Nice to meet you.\r\n";

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		proto.setState(new TeloServerState());
		return new Response(ByteBuffer.wrap(banner.getBytes())); 
	}
}
