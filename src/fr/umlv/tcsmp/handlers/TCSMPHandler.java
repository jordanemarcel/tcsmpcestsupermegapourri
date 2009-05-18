package fr.umlv.tcsmp.handlers;

import fr.umlv.tcsmp.mail.Message;

/**
 * Handlers must implement this interface.
 */
public abstract class TCSMPHandler {

	public abstract void processMessage(Message message);
	
	
	public static TCSMPHandler createHandler(String name, String param) {
		
		if (name.equals("mbox")) {
			if (param != null)
				return new MboxHandler(param);
			return new MboxHandler();
		}
		else if (name.equals("smtp")) {
			if (param != null)
				return new SmtpHandler(param);
			return new SmtpHandler();
		}
		
		return new PrintHandler();
	}
	
}
