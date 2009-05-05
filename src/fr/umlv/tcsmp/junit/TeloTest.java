package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.states.server.TeloState;

public class TeloTest {

	public static void main(String[] args) {
		ByteBuffer bb = ByteBuffer.wrap("TELO clem1.be".getBytes());
		TeloState ts = new TeloState();
		ts.processCommand(bb);
	}
}
