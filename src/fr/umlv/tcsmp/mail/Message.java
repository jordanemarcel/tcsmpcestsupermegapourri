package fr.umlv.tcsmp.mail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
//		sb.append("From: ").append(from).append("\n");
//		for (String r : rcpts) {
//			sb.append("To: ").append(r).append("\n");
//		}
		for (String s : mail) {
			if (s.equals(".\r\n") == false)
				sb.append(s.replace("\r", ""));
		}
		return sb.toString();
	}

	public static String buildMailHeader(String from, List<String> to, List<String> cc, String subject) {
		StringBuilder sb = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
		String weekDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
		String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
		String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US);
		String year = Integer.toString(calendar.get(Calendar.YEAR));
		String hour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
		String min = Integer.toString(calendar.get(Calendar.MINUTE));
		String sec = Integer.toString(calendar.get(Calendar.SECOND));
		
		sb.append("To: ");
		for(String s: to) {
			sb.append(s+",\n\t");
		}
		sb.delete(sb.length()-3, sb.length()-1);
		sb.append("\n");
		if(cc.size()>0) {
			sb.append("Cc: ");
			for(String s: cc) {
				sb.append(s+",\n\t");
			}
			sb.delete(sb.length()-3, sb.length()-1);
			sb.append("\n");
		}
		sb.append("Subject: "+subject+"\n");
		sb.append("From: "+from+"\n");
		int dst = calendar.getTimeZone().getOffset(calendar.getTimeInMillis());
		dst = dst/(1000*60*60);
		String localTime = "";
		if(dst>=0) {
			localTime = "+";
			if(dst<10) {
				localTime += "0";
			}
		} else {
			localTime = "-";
			if(dst>-10) {
				localTime += "0";
			}
			dst = dst*2 + dst; //opposite value
		}
		localTime = localTime + Integer.toString(dst) + "00";
		sb.append("Delivery-date: ");
		sb.append(weekDay+", "+day+" "+month+" "+year+" "+hour+":"+min+":"+sec+" "+localTime+"\n");
		sb.append("Content-Type: text/plain;charset=UTF-8\n");
		return sb.toString();

	}
}
