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
		this.from = from;
	}
	
	public void addRctp(String rcpt) {
		this.rcpts.add(rcpt);
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
		return mail.toString();
	}
}
