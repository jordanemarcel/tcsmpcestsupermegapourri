package fr.umlv.tcsmp.handlers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import fr.umlv.tcsmp.mail.Message;
import fr.umlv.tcsmp.utils.TCSMPLogger;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * This handler is used to write message into a
 * file by using the mailbox format.
 * 
 * Therefore, messages can be read with the best MUA
 * of the world... mutt.
 * 
 * Use mutt -f on the file generated by this handler.
 */
public class MboxHandler extends TCSMPHandler {

	private final String mboxDir;

	public MboxHandler() {
		this("/var/mail/");
	}

	public MboxHandler(String mboxDir) {
		this.mboxDir = mboxDir + "/";
	}

	@Override
	public void processMessage(Message message) {


		// for each dest. addr.
		for (String rcpt : message.getRcpts()) {

			FileOutputStream mbox = null;
			
			try {
				// retrieve user
				String user = TCSMPParser.parseUser(rcpt);
				
				// open his mailbox
				mbox = new FileOutputStream(mboxDir + user, true);
				
				// write a pseudo From.
				mbox.write("From ".getBytes());
				mbox.write(user.getBytes());
				mbox.write(" ".getBytes());
				mbox.write(new Date().toString().getBytes());
				mbox.write("\n".getBytes());
				
				// dump mail
				mbox.write(message.toString().getBytes());

			} catch(Exception e) {
				TCSMPLogger.error("Error while writing email from " + message.getFrom() + " into mbox dir.");
			}
			finally {
				if (mbox != null) {
					try {
						mbox.close();
					} catch (IOException e) {
					}
				}
			}
		}

	}
}
