package fr.umlv.tcsmp.junit;

import java.io.IOException;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;
import fr.umlv.tcsmp.tcp.handlers.PrintHandler;

public class TcpStructurePlatformTest {

	public static void main(String[] args) {
		DNSResolver resolver = new TCSMPResolver();
		try {
			TcpStructure tcpStructure = new TcpStructure(resolver);
			Protocol protocol = new Protocol(ProtocolMode.SERVER, 26);
			if(args.length==1) {
				protocol.addDomain(args[0]);
			} else {
				protocol.addDomain("foobar.com");
			}
			protocol.setMessageHandler(new PrintHandler());
			tcpStructure.processProtocol(protocol);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
