package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.server.BannerServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * Just because I was to tired to resolv conflicts \o/
 */
public class ClemServerStateTest {

	private static void printBB(Response res, ByteBuffer bb) {
		if (res == null)
			return;
		
		switch (res.getAction()) {
		case WRITE:
			if (res.getDest() != null)
				System.out.print(res.getDest() + " -> " + TCSMPParser.decode(bb));
			else
				System.out.println(TCSMPParser.decode(bb));
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
		p.addDomain("biniou.com");
		
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
		
		/**
		 * HERE WE SWITCH IN A CLIENT MODE
		 */
		// TELO
		String OK = "250 OK\r\n";
		printBB(res, bb);
		p.doIt(bb);						// send
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// FROM
		res = p.doIt(bb);				// send
		printBB(res, bb);
		p.doIt(bb);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// RCPT
		res = p.doIt(bb);				// send
		printBB(res, bb);
		res = p.doIt(bb);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// reply to client
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
		printBB(res, bb);
		p.doIt(bb);

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
		
		// OK TO THE MAIL CMD
		String DATA = "354 OK";
		printBB(res, bb);
		p.doIt(bb);						// send
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(DATA));
		bb.flip();
		
		// MAIL
		res = p.doIt(bb);				// send
		printBB(res, bb);
		p.doIt(bb);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
		
		// PKEY
		res = p.doIt(bb);				// send
		printBB(res, bb);
		res = p.doIt(bb);
		bb.clear();						// reply
		bb.put(TCSMPParser.encode(OK));
		bb.flip();
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
		printBB(res, bb);
		p.doIt(bb);
	}
}
