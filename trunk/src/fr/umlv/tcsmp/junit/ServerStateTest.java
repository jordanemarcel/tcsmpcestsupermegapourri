package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;
import java.util.Map;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.tcp.handlers.SmtpHandler;
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
		serverProtocol.addDomain("etudiant.univ-mlv.fr");
		serverProtocol.setMessageHandler(new SmtpHandler());
		
		/* CLIENT */
		Protocol clientProtocol = new Protocol(ProtocolMode.CLIENT);
		clientProtocol.setFrom("toto@titi.com");
		clientProtocol.setClientDomain("titi.com");
		clientProtocol.addRcpt("clecigne@etudiant.univ-mlv.fr");
//		clientProtocol.addRcpt("jmarce01@etudiant.univ-mlv.fr");
//		clientProtocol.addRcpt("rmasso02@etudiant.univ-mlv.fr");
		// XXX .
		clientProtocol.mail("P'tain, ca dechire du caribou.\r\n.\r\n");
		
		while(!writeReadwrite(serverProtocol, serverBB, clientProtocol, clientBB)) {};
		
		System.out.println("--------------\nOriginal server errors:");
		System.out.println(clientProtocol.getMainErrors());

		System.out.println("Particular server errors:");
		for(Map.Entry<String, StringBuilder> entry : clientProtocol.getDomainErrors().entrySet()) {
			System.out.println(entry.getKey() + ":");
			System.out.println(entry.getValue());
			System.out.println("---------");
		}
	}
	
}
