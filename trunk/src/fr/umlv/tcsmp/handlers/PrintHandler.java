package fr.umlv.tcsmp.handlers;

import fr.umlv.tcsmp.mail.Message;

/**
 * Like the PrintVisitor in compilation. It displays on stdout
 * the message which has been received.
 */
public class PrintHandler extends TCSMPHandler {

	@Override
	public void processMessage(Message message) {
		System.out.println(message);
	}
	
}
