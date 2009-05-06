package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.server.BannerState;
import fr.umlv.tcsmp.states.server.TeloState;

public class ServerStateTest {

	private static void printBB(Response res) {
		System.out.print(new String(res.getResponse().array()));
	}
	
	public static void main(String[] args) {
		
		ByteBuffer bb;
		
		/**
		 * Assume that we have received a connection.
		 */
		
		
		/**
		 * BANNER.
		 */
		Protocol p = new Protocol(new BannerState());
		printBB(p.doIt(null));
		
		/**
		 * TELO
		 */
		String telo = "TELO clem1.be";
		System.out.println(telo);
		bb = ByteBuffer.wrap(telo.getBytes());
		printBB(p.doIt(bb));

		/**
		 * FROM
		 */
		String from = "FROM <foobar@clem1.be>";
		System.out.println(from);
		bb = ByteBuffer.wrap(from.getBytes());
		printBB(p.doIt(bb));
	}
}
