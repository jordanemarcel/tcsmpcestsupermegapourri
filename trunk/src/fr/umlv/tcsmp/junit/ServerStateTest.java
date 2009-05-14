package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class ServerStateTest {

	private static boolean printBB(Response res, ByteBuffer bb) {
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
		case CLOSE:
			System.out.println("CONNECTION CLOSED");
			return true;
		}
		return false;
	}
	
	private static boolean writeReadwrite(Protocol serverProtocol, ByteBuffer serverBB, Protocol clientProtocol, ByteBuffer clientBB) {
		// WRITE
		if (printBB(serverProtocol.doIt(serverBB), serverBB)) {
			return true;
		}
		clientBB.put(serverBB);
		serverBB.clear();
		clientBB.flip();
		serverProtocol.doIt(serverBB);
		
		// READ
		if (printBB(clientProtocol.doIt(clientBB), clientBB)) {
			return true;
		}
		// WRITE
		clientProtocol.doIt(clientBB);
		serverBB.put(clientBB);
		clientBB.clear();
		serverBB.flip();
		
		return false;
	}

	public static void main(String[] args) {
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
		
		while(!writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB));
	}
}
