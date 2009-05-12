package fr.umlv.tcsmp.tcp;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;

public class KeyAttachment {
	private final ByteBuffer byteBuffer;
	private final Protocol protocol;
	
	public KeyAttachment(ByteBuffer byteBuffer, Protocol protocol) {
		this.byteBuffer = byteBuffer;
		this.protocol = protocol;
	}
	
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
}
