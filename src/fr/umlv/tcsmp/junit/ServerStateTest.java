package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.server.BannerServerState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ServerStateTest {

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
// Don't clear buffer in this case because the other end uses it
//		bb.clear(); /* assume bb has been consumed */
	}
	
	private static void writeReadwrite(Protocol serverProtocol, ByteBuffer serverBB, Protocol clientProtocol, ByteBuffer clientBB) {
		// WRITE
		printBB(serverProtocol.doIt(serverBB), serverBB);
		clientBB.put(serverBB);
		serverBB.clear();
		clientBB.flip();
		serverProtocol.doIt(serverBB);
		
		// READ
		printBB(clientProtocol.doIt(clientBB), clientBB);
		// WRITE
		clientProtocol.doIt(clientBB);
		serverBB.put(clientBB);
		clientBB.clear();
		serverBB.flip();
	}

	public static void main(String[] args) throws InterruptedException {
		ByteBuffer serverBB = ByteBuffer.allocate(1024);
		ByteBuffer clientBB = ByteBuffer.allocate(1024);
		
		/* SERVER */
		Protocol serverProtocol = new Protocol(ProtocolMode.SERVER);
		serverProtocol.addDomain("biniou.com");
		/* CLIENT */
		Protocol clientProtocol = new Protocol(ProtocolMode.CLIENT);
		clientProtocol.setFrom("toto@titi.com");
		clientProtocol.setClientDomain("titi.com");
		clientProtocol.getRecpts().add("billou@biniou.com");
		// XXX .
		clientProtocol.mail("P'tain, ca dechire du caribou.\r\n.\r\n");
		
		/**
		 * BANNER.
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * TELO
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * FROM
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * RCPT
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * APZL
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * MAIL
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);
		
		/**
		 * DATA
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);

		/**
		 * PKEY
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);
		
		/**
		 * QUIT
		 */
		writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB);
		
	}
}
