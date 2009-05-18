package fr.umlv.tcsmp.main;

import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class ServerTCSMP {

	public static void main(String[] args) {
		if (args.length != 1 && args.length != 2) {
			System.err.println("usage: java serverTCSMP domainfile [dnsaddr]");
			System.exit(1);
		}

		try {
			TCSMPResolver tr = new TCSMPResolver(args[0]);
			if (args.length == 2)
				tr.setServer(args[1]);
			TcpStructure tcpStructure = new TcpStructure(tr);
			Protocol protocol = new Protocol(ProtocolMode.SERVER, 2626);
			tcpStructure.processProtocol(protocol);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
