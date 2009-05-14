package fr.umlv.tcsmp.junit;

import java.io.IOException;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class TcpStructurePlatformTest {

	public static void main(String[] args) {
		DNSResolver resolver = new TCSMPResolver();
		try {
			TcpStructure tcpStructure = new TcpStructure(resolver);
			Protocol protocol = new Protocol(ProtocolMode.SERVER);
			tcpStructure.processProtocol(protocol);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
