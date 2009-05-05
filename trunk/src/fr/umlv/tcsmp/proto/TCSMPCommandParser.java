package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

public class TCSMPCommandParser {

	public static String[] parse(ByteBuffer bb) {
		return new String(bb.array()).split("\\s+");
	}
}
