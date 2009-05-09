package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.server.BannerState;
import fr.umlv.tcsmp.states.server.TeloState;

public class ServerStateTest {

	private static void printBB(Response res) {
		if (res.hasDest()) {
			System.out.println(res.getDest() + " -> " + new String(res.getResponse().array()));
		}
		else {
			System.out.print(new String(res.getResponse().array()));
		}
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

		/**
		 * RCPT
		 */
		String rcpt = "RCPT <foobar@biniou.com>";
		System.out.println(rcpt);
		bb = ByteBuffer.wrap(rcpt.getBytes());
		printBB(p.doIt(bb));
		
		/**
		 * APZL
		 */
		String apzl = "APZL";
		System.out.println(apzl);
		bb = ByteBuffer.wrap(apzl.getBytes());
		printBB(p.doIt(bb));
		
		/**
		 * MAIL
		 */
		String mail = "MAIL";
		System.out.println(mail);
		bb = ByteBuffer.wrap(mail.getBytes());
		printBB(p.doIt(bb));
		
		/**
		 * DATA
		 */
		String data = "TUPUDUKU SERVER TCSMP.\r\n.\r\n";
		System.out.println(data);
		bb = ByteBuffer.wrap(data.getBytes());
		printBB(p.doIt(bb));
		
		/**
		 * PKEY
		 */
		String pkey = "PKEY mydomain 20,20 MOULLLLEFRIIIITTTE\r\n";
		System.out.println(pkey);
		bb = ByteBuffer.wrap(pkey.getBytes());
		printBB(p.doIt(bb));
		
		
	}
}
