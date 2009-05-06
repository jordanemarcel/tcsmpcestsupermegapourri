package fr.umlv.tcsmp.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;


public class TCSMPResolver {
	
	/* DNS server to ask */
	private final String server;

	private static final HashMap<String, InetAddress> hosts = new HashMap<String, InetAddress>();
	
	{
		try {
			hosts.put("biniou", InetAddress.getByName("192.168.0.1"));
			hosts.put("pouet", InetAddress.getByName("192.168.0.2"));
			hosts.put("foo", InetAddress.getByName("192.168.0.3"));
		} catch (UnknownHostException e) {}
	}
	
	public TCSMPResolver() {
		this("127.0.0.1");
	}

	public TCSMPResolver(String server) {
		this.server = server;
	}
	
	public InetAddress resolv(String host) throws UnknownHostException {
		if (hosts.containsKey(host)) {
			return hosts.get(host);
		}
		throw new UnknownHostException();
	}
}
