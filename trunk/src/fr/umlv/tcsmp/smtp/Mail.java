package fr.umlv.tcsmp.smtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * Class which is able to send mail through the SMTP protocol.
 */
public class Mail {

	private final String smtpServer;
	private final int port;
	private Socket conn;
	private PrintStream out;
	private BufferedReader in;

	public Mail(String smtpServer, int port) {
		this.smtpServer = smtpServer;
		this.port = port;
	}

	public Mail(String smtpServer) {
		this(smtpServer, 25);
	}

	private void sendCommand(String cmd, boolean check) throws IOException {
		String reply;
		out.println(cmd);
		if (check) {
			reply = in.readLine(); 
			if(reply.startsWith("5")) {
				throw new IllegalAccessError(reply);
			}
		}
	}

	public void sendMail(String from, String to, String subject, String message) {
		openConn();
		String reply;
		try
		{
			sendCommand("HELO foo", true);
			sendCommand("MAIL FROM: "+from, true);
			sendCommand("RCPT TO: <"+to+">", true);
			sendCommand("DATA", true);
			sendCommand("From: "+from, false);
			sendCommand("To: "+to, false);
			if(subject != null)
				sendCommand("Subject: "+subject, false);
			sendCommand("", false);
			sendCommand(message, false);
			sendCommand("\r\n.\r\n", false);
			out.flush();
			reply = in.readLine();
			if(reply.startsWith("5"))
			{
				throw new IllegalAccessError(reply);
			}
			sendCommand("QUIT", true);
			closeConn();
		}
		catch(Exception any)
		{
			throw new IllegalAccessError(any.getMessage());
		}
	}

	private void openConn()	{
		try
		{
			conn = new Socket(smtpServer, port);
			out = new PrintStream(conn.getOutputStream());
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			if(in.readLine().startsWith("4"))
				throw new IllegalAccessError();
		}
		catch(Exception e)  
		{
			throw new IllegalAccessError();
		}
	}


	public void closeConn()
	{
		try
		{
			in.close();
			out.close();
			conn.close();
		}
		catch(Exception e)
		{
		}
	}

	public static void main(String Args[])
	{
		
	}
}