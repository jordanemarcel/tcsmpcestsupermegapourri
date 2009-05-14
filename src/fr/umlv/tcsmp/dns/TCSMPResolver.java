package fr.umlv.tcsmp.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;


public class TCSMPResolver implements DNSResolver {

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

		try {
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			env.put("java.naming.provider.url", "dns://" + server + "/");
			DirContext ctx = new InitialDirContext(env);
			Attributes attrs = ctx.getAttributes(host, new String[] { "MX" });
			for (NamingEnumeration<? extends Attribute> ae = attrs.getAll(); ae.hasMoreElements();) {
				Attribute attr = ae.next();
				return InetAddress.getByName(attr.get(0).toString().split("\\s+")[1]);
			}
			ctx.close();
		} catch (Exception e) {
		}

		throw new UnknownHostException();
	}
	
	/*
	public static void main(String[] args) throws UnknownHostException {
		
		System.out.println(new TCSMPResolver("192.168.1.254").resolv("clem1.be"));
		
	}
	*/
}
