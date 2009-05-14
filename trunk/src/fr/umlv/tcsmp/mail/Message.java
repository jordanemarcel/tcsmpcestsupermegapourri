package fr.umlv.tcsmp.mail;

import java.util.ArrayList;

public class Message {

	private String from;
	private ArrayList<String> rcpts;
	private final ArrayList<String> mail;
	
	public Message() {
		mail = new ArrayList<String>();
		rcpts = new ArrayList<String>();
	}
	
	public void copy(Message message) {
		if (message.from != null)
			this.setFrom(message.from);
		for (String rcpt : message.rcpts)
			addRctp(rcpt);
		for (String data : message.mail)
			data(data);
	}
	
	public void data(String data) {
		mail.add(data);
	}
	
	public void setFrom(String from) {
		this.from = from.toLowerCase();
	}
	
	public void addRctp(String rcpt) {
		this.rcpts.add(rcpt.toLowerCase());
	}
	
	public ArrayList<String> getMail() {
		return mail;
	}
	
	public String getLongMail() {
		StringBuilder sb = new StringBuilder();

		for (String s : mail) {
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	public String getFrom() {
		return from;
	}
	
	public ArrayList<String> getRcpts() {
		return rcpts;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("From: ").append(from).append("\n");
		for (String r : rcpts) {
			sb.append("To: ").append(r).append("\n");
		}
		for (String s : mail) {
			if (s.equals(".\r\n") == false)
				sb.append(s.replace("\r", ""));
		}
		return sb.toString();
	}
}
