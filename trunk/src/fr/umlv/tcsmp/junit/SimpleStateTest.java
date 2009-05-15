package fr.umlv.tcsmp.junit;

import java.nio.ByteBuffer;
import java.util.Map;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.client.MailClientState;
import fr.umlv.tcsmp.states.client.PkeyClientState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class SimpleStateTest {

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
		// server goes in WRITE 
		if (printBB(serverProtocol.doIt(serverBB), serverBB)) {
			return true;
		}
		clientBB.put(serverBB);
		serverBB.clear();
		clientBB.flip();
		// server goes in READ
		if (serverProtocol.doIt(serverBB).getAction() == ResponseAction.CLOSE) {
			return true;
		}
		
		// client goes in WRITE
		if (printBB(clientProtocol.doIt(clientBB), clientBB)) {
			return true;
		}

		
		if (clientProtocol.getState().getClass() == PkeyClientState.class) {
			// simulate write failure
			if (clientProtocol.cancel(clientBB).getAction() == ResponseAction.CLOSE) {
				return true;
			}
		}
		else {
			// client goes in READ
			if (clientProtocol.doIt(clientBB).getAction() == ResponseAction.CLOSE) {
				return true;
			}
		}
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
		clientProtocol.getRecpts().add("jojo@biniou.com");
		clientProtocol.getRecpts().add("clem@biniou.com");
		// XXX .
		clientProtocol.mail("P'tain, ca dechire du caribou.\r\n.\r\n");
		
		// init client banner state
		clientProtocol.doIt(clientBB);
		
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
