package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.server.BannerServerState;
import fr.umlv.tcsmp.states.server.TeloServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ServerStateTest {

	private static void printBB(Response res, ByteBuffer bb) {
		if (res.hasDest()) {
			System.out.print(res.getDest() + " -> " + new String(bb.array()));
		}
		else {
			System.out.print(new String(bb.array()));
		}
	}

	public static void main(String[] args) {

		ByteBuffer bb = ByteBuffer.allocate(1024);

		/**
		 * Assume that we have received a connection.
		 */


		/**
		 * BANNER.
		 */
		Protocol p = new Protocol(new BannerServerState());
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * TELO
		 */
		String telo = "TELO clem1.be\r\n";
		System.out.print(telo);
		bb.clear();
		bb.put(TCSMPParser.encode(telo));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);

		/**
		 * FROM
		 */
		String from = "FROM <foobar@clem1.be>\r\n";
		System.out.print(from);
		bb.clear();
		bb.put(TCSMPParser.encode(from));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * RCPT
		 */
		String rcpt = "RCPT <foobar@biniou.com>\r\n";
		System.out.print(rcpt);
		bb.clear();
		bb.put(TCSMPParser.encode(rcpt));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * APZL
		 */
		String apzl = "APZL\r\n";
		System.out.print(apzl);
		bb.clear();
		bb.put(TCSMPParser.encode(apzl));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * MAIL
		 */
		String mail = "MAIL\r\n";
		System.out.print(mail);
		bb.clear();
		bb.put(TCSMPParser.encode(mail));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * DATA
		 */
		String data = "TUPUDUKU SERVER TCSMP.\r\n.\r\n";
		System.out.print(data);
		bb.clear();
		bb.put(TCSMPParser.encode(data));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * PKEY
		 */
		String pkey = "PKEY mydomain 20,20 MOULLLLEFRIIIITTTE\r\n";
		System.out.print(pkey);
		bb.clear();
		bb.put(TCSMPParser.encode(pkey));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		
		/**
		 * QUIT
		 */
		String quit = "QUIT\r\n";
		System.out.print(quit);
		bb.clear();
		bb.put(TCSMPParser.encode(quit));
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
	}
}
 