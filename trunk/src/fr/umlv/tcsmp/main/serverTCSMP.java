package fr.umlv.tcsmp.main;

import java.io.IOException;

import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class serverTCSMP {

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("usage: java serverTPOP domainfile");
			System.exit(1);
		}
		
		try {
			TcpStructure tcpStructure = new TcpStructure(new TCSMPResolver(args[1]));
			Protocol protocol = new Protocol(ProtocolMode.SERVER, 26);
			protocol.addDomain(args[0]);
			tcpStructure.processProtocol(protocol);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
