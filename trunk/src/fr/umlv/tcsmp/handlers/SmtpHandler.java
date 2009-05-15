package fr.umlv.tcsmp.handlers;

import java.net.InetAddress;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.mail.Message;
import fr.umlv.tcsmp.smtp.Mail;

/**
 * This handler can be used to relay Message to
 * a real SMTP server. 
 */
public class SmtpHandler implements TCSMPHandler {

	private final String smtpServer;

	public SmtpHandler() {
		smtpServer = "etudiant.univ-mlv.fr";
	}
	
	public SmtpHandler(String server) {
		smtpServer = server;
	}
	
	@Override
	public void processMessage(Message message) {

		for (String rcpt : message.getRcpts()) {

			Mail mail;
			
			try {

				InetAddress addr = InetAddress.getByName(smtpServer);
				mail = new Mail(addr);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}

			mail.sendMail(message.getFrom(), rcpt, "Fook", message.getLongMail());
			mail.closeConn();
		}
	}

}
