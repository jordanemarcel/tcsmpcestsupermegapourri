package fr.umlv.tcsmp.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface DNSResolver {
	
	public InetAddress resolv(String host) throws UnknownHostException;

	public void setServer(String server);
}
