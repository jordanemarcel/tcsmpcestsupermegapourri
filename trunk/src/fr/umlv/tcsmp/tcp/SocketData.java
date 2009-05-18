package fr.umlv.tcsmp.tcp;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;

/**
 * This class represents a list of clients of a protocol session, including the
 * original client that initialized the session.
 */
public class SocketData {
	/** The client that initialized the session */
	private SocketChannel originalClient;
	/** A map of sockets and their corresponding domain */
	private final HashMap<String, SocketChannel> domainSocketMap = new HashMap<String, SocketChannel>();
	
	/**
	 * Constructs a new SocketData and sets the original client
	 * @param originalClient - the original client
	 */
	public SocketData(SocketChannel originalClient) {
		this.originalClient = originalClient;
	}
	
	/**
	 * Returns the original client of the session
	 * @return - the original client
	 */
	public SocketChannel getOriginalClient() {
		return originalClient;
	}
	
	/**
	 * Returns the socket that correspond with the given domain
	 * @param domain - the given domain
	 * @return - The SocketChannel that correspond with the given domain, null otherwise.
	 */
	public SocketChannel getSocket(String domain) {
		return domainSocketMap.get(domain);
	}
	
	/**
	 * Adds a new Domain/Socket couple in the map
	 * @param socketChannel - the given SocketChannel
	 * @param domain - the given domain
	 */
	public void putSocket(SocketChannel socketChannel, String domain) {
		if(socketChannel==originalClient) {
			return;
		}
		domainSocketMap.put(domain, socketChannel);
	}
	
	/**
	 * Sets the original client (uses by the Client MODE)
	 * @param originalClient
	 */
	public void setOriginalClient(SocketChannel originalClient) {
		this.originalClient = originalClient;
	}
	
	/**
	 * Return the Collection of connected clients
	 * @return - the connection of connected clients
	 */
	public Collection<SocketChannel> getClients() {
		return domainSocketMap.values();
	}

}
