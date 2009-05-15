package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.umlv.tcsmp.puzzle.Puzzle;



public class TCSMPParser {

	private static final Charset charset = Charset.forName("ASCII");
	private static Pattern emailrx = Pattern.compile("<(.*?)@(.*?\\.\\p{Alpha}{2,3})>");
	private static Pattern emailrx2 = Pattern.compile("(.*?)@(.*?\\.\\p{Alpha}{2,3})");
	
	/**
	 * Simply split command found in bytebuffer. 
	 */
	public static String[] parseCommand(ByteBuffer bb) {
		return new String(TCSMPParser.decode(bb)).split("\\s+");
	}

	/**
	 * Split multiline answer found in bytebuffer. 
	 */
	public static boolean parseAnswer(ByteBuffer bb, List<String> list) {
		String[] stringArray = new String(charset.decode(bb).toString()).split("\r\n");
		for(String string: stringArray) {
			if (string.charAt(3) == '-') {
				list.add(string.substring(0, 3));
				list.add(string.substring(4));
			}
			else {
				list.add(string);
			}
		}
				
		String lastString = list.get(list.size() -1);
		if (lastString.charAt(3) != '-') {
			list.remove(lastString);
			list.add(lastString.substring(0, 3));
			list.add(lastString.substring(4));
			return true;
		}
		
		return false;
	}
	
	/**
	 * Decode an array of bytes into a String, using the good charset
	 * @param bytes bytes array to decode
	 * @return the decoded String
	 */
	public static String decode(byte[] bytes) {
		return charset.decode(ByteBuffer.wrap(bytes)).toString();
	}
	
	/**
	 * Decode a ByteBuffer into a String, using the good charset
	 * @param ByteBuffer to decode
	 * @return the decoded String
	 */
	public static String decode(ByteBuffer bb) {
		try {
			byte[] bytes = new byte[bb.limit()];
			bb.get(bytes);
			return charset.decode(ByteBuffer.wrap(bytes)).toString();
		} finally {
			bb.flip();
		}
	}
	
	/**
	 * Encode a String into an array of bytes, using the good charset
	 * @param the String to encode
	 * @return the encoded byte buffer
	 */
	public static ByteBuffer encode(String victim) {
		return charset.encode(victim);
	}

	/**
	 * @return the domain associated to the email address. 
	 */
	public static String parseDomain(String mail) throws ParseException {
		Matcher matcher = emailrx.matcher(mail);
		if(matcher.matches() && matcher.groupCount() == 2)
			return matcher.group(2).toLowerCase();

		matcher = emailrx2.matcher(mail);
		if(matcher.matches() && matcher.groupCount() == 2)
			return matcher.group(2).toLowerCase();
		
		throw new ParseException(mail + " is not a valid email address", 0);
	}
	
	public static Puzzle parsePuzzleDesc(String dimensions, String description) {
		int width = Integer.parseInt(dimensions.substring(0, dimensions.indexOf(',')));
		int height = Integer.parseInt(dimensions.substring(dimensions.indexOf(',') + 1));
		return new Puzzle(width, height, description);
	}

	/**
	 * @return the user associated to the email address. 
	 */
	public static String parseUser(String mail) throws ParseException {
		Matcher matcher = emailrx.matcher(mail);
		if(matcher.matches() && matcher.groupCount() == 2)
			return matcher.group(1).toLowerCase();
		
		matcher = emailrx2.matcher(mail);
		if(matcher.matches() && matcher.groupCount() == 2)
			return matcher.group(1).toLowerCase();

		throw new ParseException(mail + " is not a valid email address", 0);
	}

	/**
	 * Create a multi line response with the responses found in responses
	 */
	public static void multilinize(ArrayList<String> responses) {
		for (int i = 0; i < responses.size() - 1; i++) {
			String res = responses.get(i);
			responses.set(i, res.replaceFirst("\\s", "-"));
		}
		// last response should contains the space after code.
	}
	
	/**
	 * Return an array of string corresponding of the line in the response
	 * @param response if the lines to split
	 */
	public static String[] slipResponseLine(String response) {
		String[] stringArray = response.split("\r\n");
		
		if (stringArray.length == 0) {
			throw new AssertionError("response must at least contain one line ended by CRLF");
		}
		
		if (stringArray.length == 1) {
			stringArray[0] += "\r\n";
			return stringArray;
		}
		
		String[] responses = new String[stringArray.length];
		
		for (int i = 0; i < stringArray.length - 1; i++) {
			responses[i] = stringArray[i].replaceFirst("-", "\\s") + "\r\n";
		}
		
		responses[stringArray.length-1] = stringArray[stringArray.length-1] + "\r\n";
		
		return responses;
	}
	
	/**
	 * Tells whether a specified domain appears in any receipts recorded in the given list.
	 * @param receipts list of correct email addresses
	 * @param domain String domain to look for
	 * @return true if domain was found, false otherwise
	 */
	public static boolean lookupDomain(List<String> receipts, String domain) {
		for(String s : receipts) {
			if (s.endsWith(domain)) {
				return true;
			}
		}
		return false;
	}
}
