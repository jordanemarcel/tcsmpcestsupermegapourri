package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;

public class ErrorReply {

	public static ByteBuffer unknowCommand(String expected, String received) {
		return ByteBuffer.wrap(new String("500 Invalid command (Found is " + received + " but expected is " + expected).getBytes());
	}
}
