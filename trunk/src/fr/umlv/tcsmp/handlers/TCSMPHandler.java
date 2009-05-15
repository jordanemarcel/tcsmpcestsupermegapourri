package fr.umlv.tcsmp.handlers;

import fr.umlv.tcsmp.mail.Message;

/**
 * Handlers must implement this interface.
 */
public interface TCSMPHandler {

	public void processMessage(Message message);
	
}
