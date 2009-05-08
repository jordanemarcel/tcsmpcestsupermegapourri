package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TCSMPParser {

	private static Pattern emailrx = Pattern.compile("<(\\w+)@(\\w+\\.\\p{Alpha}{2,3})>");

	/**
	 * Simply split command found in bytebuffer. 
	 */
	public static String[] parse(ByteBuffer bb) {
		return new String(bb.array()).split("\\s+");
	}

	/**
	 * @return the domain associated to the email address. 
	 */
	public static String parseDomain(String mail) throws ParseException {
		Matcher matcher = emailrx.matcher(mail);
		if(matcher.matches())
			return matcher.group(2);
		
		throw new ParseException(mail + " is not a valid email address", 0);
	}
	
	/**
	 * @return the user associated to the email address. 
	 */
	public static String parseUser(String mail) throws ParseException {
		Matcher matcher = emailrx.matcher(mail);
		if(matcher.matches())
			return matcher.group(1);
		
		throw new ParseException(mail + " is not a valid email address", 0);
	}
	
}
