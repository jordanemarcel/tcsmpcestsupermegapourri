package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * Emulate a CLIENT <----> RELAY <----> SERVER communication
 */
public class RelayStateTest {

	private static void printBB(Response res, ByteBuffer bb) {
		if (res == null)
			return;
		
		switch (res.getAction()) {
		case WRITE:
			if (res.getDest() != null)
				System.out.print(res.getDest() + " -> " + TCSMPParser.decode(bb));
			else
				System.out.print(TCSMPParser.decode(bb));
			break;
		case RELAYALL:
			System.out.print("ALL" + " -> " + TCSMPParser.decode(bb));
			break;
		}
//		bb.clear(); /* assume bb has been consumed */
	}

	public static void main(String[] args) throws InterruptedException {
		ByteBuffer serverBB = ByteBuffer.allocate(1024);
		ByteBuffer clientBB = ByteBuffer.allocate(1024);
		ByteBuffer relayBB = ByteBuffer.allocate(1024);
		
		/* RELAY */
		Protocol relayProtocol = new Protocol(ProtocolMode.SERVER, null);
		relayProtocol.addDomain("foobar.be");
		
		/* DEST SERVER */
		Protocol serverProtocol = new Protocol(ProtocolMode.SERVER, null);
		serverProtocol.addDomain("biniou.com");
		
		/* CLIENT */
		Protocol clientProtocol = new Protocol(ProtocolMode.CLIENT);
		clientProtocol.setFrom("toto@titi.com");
		clientProtocol.setClientDomain("titi.com");
		clientProtocol.getRecpts().add("billou@biniou.com");
//		clientProtocol.getRecpts().add("jojo@biniou.com");
//		clientProtocol.getRecpts().add("clem@biniou.com");
		clientProtocol.mail("P'tain, ca dechire du caribou.\r\n.\r\n");
		
		Response res;
		
		/**
		 * BANNER.
		 */
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);

		/**
		 * TELO
		 */
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(relayBB);
		
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);

		/**
		 * FROM
		 */
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(relayBB);
		
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);
		

		
		/**
		 * RCPT
		 */
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(clientBB);
		
		res = relayProtocol.doIt(relayBB);
		
		if (res.getDest() == null)
			throw new AssertionError("gni");
		
		/**
		 * RELAY BANNER.
		 */
		res = serverProtocol.doIt(serverBB);
		System.out.print("\t");
		printBB(res, serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);

		/**
		 * RELAY TELO
		 */
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);

		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);
				
		// RELAY FROM
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);

		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);
		
		// RELAY RCTP
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);

		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);

		res = relayProtocol.doIt(relayBB);
		printBB(res, relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);

		/**
		 * APZL
		 */
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(clientBB);

		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);
		
		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);
		
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);
		
		
		/**
		 * MAIL
		 */
		
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(clientBB);
		
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);

		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(clientBB);
		
		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);
		
		/**
		 * PKEY
		 */
		
		printBB(clientProtocol.doIt(clientBB), clientBB);
		relayBB.put(clientBB);
		clientBB.clear();
		relayBB.flip();
		clientProtocol.doIt(clientBB);
		
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);
		
		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);
		
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);
		
		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);
		
		res = relayProtocol.doIt(relayBB);
		System.out.print("\t");
		printBB(res, relayBB);
		serverBB.put(relayBB);
		relayBB.clear();
		serverBB.flip();
		relayProtocol.doIt(relayBB);
		
		System.out.print("\t");
		printBB(serverProtocol.doIt(serverBB), serverBB);
		relayBB.put(serverBB);
		serverBB.clear();
		relayBB.flip();
		serverProtocol.doIt(serverBB);

		printBB(relayProtocol.doIt(relayBB), relayBB);
		clientBB.put(relayBB);
		relayBB.clear();
		clientBB.flip();
		relayProtocol.doIt(relayBB);
		
//		/**
//		 * APZL
//		 */
//		String apzl = "APZL\r\n";
//		System.out.print(apzl);
//		relayBB.clear();
//		relayBB.put(TCSMPParser.encode(apzl));
//		relayBB.flip();
//		res = relayProtocol.doIt(relayBB);
//		
//		System.out.print("\t");
//		printBB(res, relayBB);
//		
//		res = relayProtocol.doIt(relayBB);
//		
//		if (res.getAction() != ResponseAction.READ)
//			throw new AssertionError("Coin");
//		
//		String PZL = "215 foobar.com 4,4 2isxausiba9umd4assuksdrs90nd49v0uyn2rfyynewfvglenwyuy1sww2m1l652\r\n";
//		relayBB.clear();						// reply
//		relayBB.put(TCSMPParser.encode(PZL));
//		relayBB.flip();
//		System.out.print("\t" + PZL);
//		res = relayProtocol.doIt(relayBB);
//		printBB(res, relayBB);
//		relayProtocol.doIt(relayBB); // yes written
//
//		/**
//		 * MAIL
//		 */
//		String mail = "MAIL\r\n";
//		System.out.print(mail);
//		relayBB.clear();
//		relayBB.put(TCSMPParser.encode(mail));
//		relayBB.flip();
//		res = relayProtocol.doIt(relayBB);
//		printBB(res, relayBB);
//		relayProtocol.doIt(relayBB);
//
//		/**
//		 * DATA
//		 */
//		String data = "TUPUDUKU SERVER TCSMP.\r\n.\r\n";
//		System.out.print(data);
//		relayBB.clear();
//		relayBB.put(TCSMPParser.encode(data));
//		relayBB.flip();
//		res = relayProtocol.doIt(relayBB);
//		printBB(res, relayBB);
//		relayProtocol.doIt(relayBB);
//
//		/**
//		 * PKEY
//		 */
//		String pkey = "PKEY foobar.com 4,4 ci1k31p09puqouplkyvb0vw5qwvblv4pbftf5tewbeoypoocf4m4wmvbyv6tc65j\r\n";
//		System.out.print(pkey);
//		relayBB.clear();
//		relayBB.put(TCSMPParser.encode(pkey));
//		relayBB.flip();
//		res = relayProtocol.doIt(relayBB);
//		
//		if (res.getDest() == null)
//			throw new AssertionError("gni");
//		
//		// RELAY MAIL
//		System.out.print("\t");
//		printBB(res, relayBB);
//		relayProtocol.doIt(relayBB);
//		String DATA = "354 Give me your mail dude\r\n";
//		System.out.print("\t");
//		System.out.print(DATA);
//		relayBB.clear();						// reply
//		relayBB.put(TCSMPParser.encode(DATA));
//		relayBB.flip();
//		
//		// RELAY DATA
//		System.out.print("\t");
//		printBB(relayProtocol.doIt(relayBB), relayBB);
//		relayProtocol.doIt(relayBB);
//		System.out.print("\t");
//		System.out.print(OK);
//		relayBB.clear();						// reply
//		relayBB.put(TCSMPParser.encode(OK));
//		relayBB.flip();
//		
//		// RELAY PKEY
//		System.out.print("\t");
//		printBB(relayProtocol.doIt(relayBB), relayBB);
//		relayProtocol.doIt(relayBB);
//		System.out.print("\t");
//		OK = "216 DUDE YOU'RE RIGHT\r\n";
//		System.out.print(OK);
//		relayBB.clear();						// reply
//		relayBB.put(TCSMPParser.encode(OK));
//		relayBB.flip();
//		
//		res = relayProtocol.doIt(relayBB);
//		
//		if (res.getDest() != null)
//			throw new AssertionError("gnou");
//		
//		printBB(res, relayBB);
//		
//
//		/**
//		 * QUIT
//		 */
//		String quit = "QUIT\r\n";
//		System.out.print(quit);
//		relayBB.clear();
//		relayBB.put(TCSMPParser.encode(quit));
//		relayBB.flip();
//		res = relayProtocol.doIt(relayBB);
//		
//		if (res.getAction() != ResponseAction.RELAYALL)
//			throw new AssertionError("foo");
//		
//		// RELAY QUIT
//		System.out.print("\t");
//		printBB(res, relayBB);
//		relayProtocol.doIt(relayBB);
//		OK = "200 OK\r\n";
//		System.out.print("\t");
//		System.out.print(OK);
//		relayBB.clear();						// reply
//		relayBB.put(TCSMPParser.encode(OK));
//		relayBB.flip();
//		printBB(relayProtocol.doIt(relayBB), relayBB);
	}
}
