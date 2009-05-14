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
		message.setFrom(from);
		for (String rcpt : rcpts)
			message.addRctp(rcpt);
		for (String data : mail)
			message.data(data);
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
		String data = "";
		for (String s : mail) {
			System.out.println(data);
			data += s;
		}
		return data;
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
