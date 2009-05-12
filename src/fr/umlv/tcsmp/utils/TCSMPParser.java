package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.umlv.tcsmp.puzzle.Puzzle;



public class TCSMPParser {

	private static final Charset charset = Charset.forName("ASCII");
	private static Pattern emailrx = Pattern.compile("<(\\w+)@(\\w+\\.\\p{Alpha}{2,3})>");

	/**
	 * Simply split command found in bytebuffer. 
	 */
	public static String[] parseCommand(ByteBuffer bb) {
		return new String(bb.array()).split("\\s+");
	}

	/**
	 * Split multiline answer found in bytebuffer. 
	 */
	public static boolean parseAnswer(ByteBuffer bb, List<String> list) {
		String[] stringArray = new String(bb.array()).split("\\s+");
		for(String string: stringArray) {
			list.add(string);
		}

		String lastString = list.get(list.size() -1); 
		if (lastString.charAt(3) != '-') {
			return false;
		}

		list.remove(list.size() - 1);
		list.add(lastString.substring(0, 3));
		list.add(lastString.substring(3));
		return true;
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
	 * Encode a String into an array of bytes, using the good charset
	 * @param the String to encode
	 * @return bytes the encoded bytes array
	 */
	public static byte[] encode(String victim) {
		return charset.encode(victim).array();
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
		if(matcher.matches())
			return matcher.group(1);

		throw new ParseException(mail + " is not a valid email address", 0);
	}

}
