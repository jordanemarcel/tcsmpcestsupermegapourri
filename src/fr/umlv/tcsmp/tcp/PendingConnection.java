package fr.umlv.tcsmp.tcp;

import java.nio.channels.SelectionKey;

public class PendingConnection {
	private static final long maxPendingTime = 3000;
	private final SelectionKey selectionKey;
	private final long startTime;
	
	public PendingConnection(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
		this.startTime = System.currentTimeMillis();
	}
	
	public boolean isTimedOut() {
		long currentTime = System.currentTimeMillis();
		return (currentTime - startTime) > maxPendingTime;
	}
	
	public SelectionKey getSelectionKey() {
		return selectionKey;
	}
}
