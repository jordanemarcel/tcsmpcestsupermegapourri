package fr.umlv.tcsmp.test;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * Emulate a CLIENT <----> RELAY <----> SERVER communication
 */
public class RelayServerStateTest {

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
		bb.clear(); /* assume bb has been consumed */
	}

	public static void main(String[] args) throws InterruptedException {

		ByteBuffer bb = ByteBuffer.allocate(1024);
		Protocol p = new Protocol(ProtocolMode.SERVER);
		p.addDomain("foobar.be");
		
		/**
		 * BANNER.
		 */
		Response res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb);

		/**
		 * TELO
		 */
		String telo = "TELO clem1.be\r\n";
		System.out.print(telo);
		bb.clear();
		bb.put(TCSMPParser.encode(telo));
		bb.flip();
		res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb);

		/**
		 * FROM
		 */
		String from = "FROM <foobar@clem1.be>\r\n";
		System.out.print(from);
		bb.clear();
		bb.put(TCSMPParser.encode(from));
		bb.flip();
		res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb);

		/**
		 * RCPT
		 */
		String rcpt = "RCPT <foobar@foobar.com>\r\n";
		System.out.print(rcpt);
		bb.clear();
		bb.put(TCSMPParser.encode(rcpt));
		bb.flip();
		res = p.doIt(bb);
		
		if (res.getDest() == null)
			throw new AssertionError("gni");
		
		System.out.print("\t");
		String BANNER = "220 Coucou c'est nous";
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(BANNER));
		bb.flip();
		res = p.doIt(bb);
		
		// RELAY TELO
		// init banner client state
//		p.doIt(bb);
		System.out.print("\t");
		printBB(res, bb);
		p.doIt(bb);
		String OK = "250 OK\r\n";
		System.out.print("\t");
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// RELAY FROM
		System.out.print("\t");
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		System.out.print("\t");
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// RELAY RCTP
		System.out.print("\t");
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		System.out.print("\t");
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		res = p.doIt(bb);
		
		if (res.getDest() != null)
			throw new AssertionError("gnou");
		
		printBB(res, bb);
		
		
		/**
		 * APZL
		 */
		String apzl = "APZL\r\n";
		System.out.print(apzl);
		bb.clear();
		bb.put(TCSMPParser.encode(apzl));
		bb.flip();
		res = p.doIt(bb);
		
		System.out.print("\t");
		printBB(res, bb);
		
		res = p.doIt(bb);
		
		if (res.getAction() != ResponseAction.READ)
			throw new AssertionError("Coin");
		
		String PZL = "215 foobar.com 4,4 2isxausiba9umd4assuksdrs90nd49v0uyn2rfyynewfvglenwyuy1sww2m1l652\r\n";
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(PZL));
		bb.flip();
		System.out.print("\t" + PZL);
		res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb); // yes written

		/**
		 * MAIL
		 */
		String mail = "MAIL\r\n";
		System.out.print(mail);
		bb.clear();
		bb.put(TCSMPParser.encode(mail));
		bb.flip();
		res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb);

		/**
		 * DATA
		 */
		String data = "TUPUDUKU SERVER TCSMP.\r\n.\r\n";
		System.out.print(data);
		bb.clear();
		bb.put(TCSMPParser.encode(data));
		bb.flip();
		res = p.doIt(bb);
		printBB(res, bb);
		p.doIt(bb);

		/**
		 * PKEY
		 */
		String pkey = "PKEY foobar.com 4,4 ci1k31p09puqouplkyvb0vw5qwvblv4pbftf5tewbeoypoocf4m4wmvbyv6tc65j\r\n";
		System.out.print(pkey);
		bb.clear();
		bb.put(TCSMPParser.encode(pkey));
		bb.flip();
		res = p.doIt(bb);
		
		if (res.getDest() == null)
			throw new AssertionError("gni");
		
		// RELAY MAIL
		System.out.print("\t");
		printBB(res, bb);
		p.doIt(bb);
		String DATA = "354 Give me your mail dude\r\n";
		System.out.print("\t");
		System.out.print(DATA);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(DATA));
		bb.flip();
		
		// RELAY DATA
		System.out.print("\t");
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		System.out.print("\t");
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// RELAY PKEY
		System.out.print("\t");
		printBB(p.doIt(bb), bb);
		p.doIt(bb);
		System.out.print("\t");
		OK = "216 DUDE YOU'RE RIGHT\r\n";
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		res = p.doIt(bb);
		
		if (res.getDest() != null)
			throw new AssertionError("gnou");
		
		printBB(res, bb);
		

		/**
		 * QUIT
		 */
		String quit = "QUIT\r\n";
		System.out.print(quit);
		bb.clear();
		bb.put(TCSMPParser.encode(quit));
		bb.flip();
		res = p.doIt(bb);
		
		if (res.getAction() != ResponseAction.RELAYALL)
			throw new AssertionError("foo");
		
		// RELAY QUIT
		System.out.print("\t");
		printBB(res, bb);
		p.doIt(bb);
		OK = "200 OK\r\n";
		System.out.print("\t");
		System.out.print(OK);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		printBB(p.doIt(bb), bb);
	}
}
