package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;

public class ErrorReplies {

	public static ByteBuffer unknowCommand(String expected, String received) {
		return TCSMPParser.encode(new String("500 Invalid command (Found is " + received + " but expected is " + expected + ")\r\n"));
	}
}
