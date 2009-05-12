package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.server.BannerServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ServerStateTest {

	private static void printBB(Response res, ByteBuffer bb) {
		if (res == null)
			return;
		
		switch (res.getAction()) {
		case REPLY:
			System.out.print(TCSMPParser.decode(bb));
			break;
		case RELAY:
			System.out.print(res.getDest() + " -> " + TCSMPParser.decode(bb));
			break;
		case RELAYALL:
			System.out.print("ALL" + " -> " + TCSMPParser.decode(bb));
			break;
		}
//		bb.flip(); /* assume bb has been consumed */
	}

	public static void main(String[] args) throws InterruptedException {
		ByteBuffer bb = ByteBuffer.allocate(1024);
		
		/* SERVER */
		Protocol serverProtocol = new Protocol(new BannerServerState());
		serverProtocol.addDomain("biniou.com");
		/* CLIENT */
		Protocol clientProtocol = new Protocol(new BannerClientState());
		clientProtocol.setFrom("toto@titi.com");
		clientProtocol.setClientDomain("titi.com");
		clientProtocol.getRecpts().add("billou@biniou.com");
		// XXX .
		clientProtocol.mail("P'tain, ca dechire du caribou.\r\n.\r\n");
		
		/**
		 * BANNER.
		 */
		Response res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);
		
		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * TELO
		 */
//		String telo = "TELO clem1.be\r\n";
//		System.out.print(telo);
//		bb.clear();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);
		
		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * FROM
		 */
//		String from = "FROM <foobar@clem1.be>\r\n";
//		System.out.print(from);
//		bb.clear();
//		bb.put(TCSMPParser.encode(from));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);
	
		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * RCPT
		 */
//		String rcpt = "RCPT <foobar@biniou.com>\r\n";
//		System.out.print(rcpt);
//		bb.clear();
//		bb.put(TCSMPParser.encode(rcpt));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);
		
		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * APZL
		 */
//		String apzl = "APZL\r\n";
//		System.out.print(apzl);
//		bb.clear();
//		bb.put(TCSMPParser.encode(apzl));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);
		
		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * MAIL
		 */
//		String mail = "MAIL\r\n";
//		System.out.print(mail);
//		bb.clear();
//		bb.put(TCSMPParser.encode(mail));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);

		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);
		
		/**
		 * DATA
		 */
//		String data = "TUPUDUKU SERVER TCSMP.\r\n.\r\n";
//		System.out.print(data);
//		bb.clear();
//		bb.put(TCSMPParser.encode(data));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
		serverProtocol.doIt(bb);

		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);

		/**
		 * PKEY
		 */
//		String pkey = "PKEY mydomain 20,20 MOULLLLEFRIIIITTTE\r\n";
//		System.out.print(pkey);
//		bb.clear();
//		bb.put(TCSMPParser.encode(pkey));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
//		p.doIt(bb);

		printBB(clientProtocol.doIt(bb), bb);
		clientProtocol.doIt(bb);
		
		/**
		 * QUIT
		 */
//		String quit = "QUIT\r\n";
//		System.out.print(quit);
//		bb.clear();
//		bb.put(TCSMPParser.encode(quit));
//		bb.flip();
		res = serverProtocol.doIt(bb);
		printBB(res, bb);
//		p.doIt(bb);
		
		printBB(clientProtocol.doIt(bb), bb);
	}
}
