package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.server.TeloState;

public class TeloTest {

	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.wrap("TELO clem1.be".getBytes());
		Protocol p = new Protocol(new TeloState());
		Response res = p.doIt(bb);
		System.out.println(new String(res.getResponse().array()));
	}
}
