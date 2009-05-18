package fr.umlv.tcsmp.test;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.handlers.MboxHandler;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class TcpStructurePlatformTest {

	public static void main(String[] args) {
		DNSResolver resolver = new TCSMPResolver("127.0.0.1", "/tmp/domains");
		try {
			TcpStructure tcpStructure = new TcpStructure(resolver);
			Protocol protocol = new Protocol(ProtocolMode.SERVER, 26);
			if(args.length == 1) {
				protocol.addDomain(args[0]);
			} else {
				protocol.addDomain("foobar.com");
			}
			protocol.setMessageHandler(new MboxHandler("/tmp"));
			tcpStructure.processProtocol(protocol);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
