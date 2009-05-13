package fr.umlv.tcsmp.tcp;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;

public class SocketData {
	private final SocketChannel originalClient;
	private final HashMap<String, SocketChannel> domainSocketMap = new HashMap<String, SocketChannel>();
	
	public SocketData(SocketChannel originalClient) {
		this.originalClient = originalClient;
	}
	
	public SocketChannel getOriginalClient() {
		return originalClient;
	}
	
	public SocketChannel getSocket(String domain) {
		return domainSocketMap.get(domain);
	}
	
	public Collection<SocketChannel> getClients() {
		return domainSocketMap.values();
	}

}
