package fr.umlv.tcsmp.tcp;

import java.nio.channels.SelectionKey;

/**
 * This class represents a pending connection that is waiting
 * to be accepted by a distance server. It cannot wait more than
 * its pendingTime.
 */
public class PendingConnection {
	/** the max pending time allowed by the socket */
	private static final long maxPendingTime = 3000;
	/** the selection Key associated with the pending conection */
	private final SelectionKey selectionKey;
	/** the time that the socket was created */
	private final long startTime;
	
	/**
	 * Default constructor
	 * @param selectionKey
	 */
	public PendingConnection(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
		this.startTime = System.currentTimeMillis();
	}
	
	/**
	 * Tests whether the connection is timed out or not.
	 * @return true if the conection has timed out
	 */
	public boolean isTimedOut() {
		long currentTime = System.currentTimeMillis();
		return (currentTime - startTime) > maxPendingTime;
	}
	
	/**
	 * Returns the selection key associated with the current pending connection
	 * @return the selection key
	 */
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}
}
