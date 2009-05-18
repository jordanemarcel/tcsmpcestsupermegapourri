package fr.umlv.tcsmp.main;

import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.handlers.TCSMPHandler;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class ServerTPOP {

	public static void main(String[] args) {

		if (args.length != 1 && args.length != 3) {
			System.err.println("usage: java serverTPOP domainname [handler handlerparam]");
			System.exit(1);
		}
		
		// get default handler
		TCSMPHandler handler = TCSMPHandler.createHandler("foo", null);
		
		// parse handler
		if (args.length == 3) {
			handler = TCSMPHandler.createHandler(args[1], args[2]);
		}
		
		try {
			TcpStructure tcpStructure = new TcpStructure(new TCSMPResolver());
			Protocol protocol = new Protocol(ProtocolMode.SERVER, 2626);
			protocol.addDomain(args[0]);
			protocol.setMessageHandler(handler);
			tcpStructure.processProtocol(protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
