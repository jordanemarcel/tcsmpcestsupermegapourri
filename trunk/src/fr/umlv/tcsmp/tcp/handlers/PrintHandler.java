package fr.umlv.tcsmp.tcp.handlers;

import fr.umlv.tcsmp.mail.Message;

/**
 * Like the PrintVisitor in compilation. It displays on stdout
 * the message which has been received.
 */
public class PrintHandler implements TCSMPHandler {

	@Override
	public void processMessage(Message message) {
		System.out.println(message);
	}
	
}
