package fr.umlv.tcsmp.utils;

import java.nio.ByteBuffer;

public class ErrorReplies {

	public static ByteBuffer unknowCommand(String expected, String received) {
		return TCSMPParser.encode(new String("500 Invalid command (Found is " + received + " but expected is " + expected + ")\r\n"));
	}
	
	public static ByteBuffer timeoutError() {
		return TCSMPParser.encode("421 Error: timeout exceeded\r\n");
	}
	
	public static ByteBuffer unexpectedError() {
		return TCSMPParser.encode("421 Error: unexpected error\r\n");
	}
}
