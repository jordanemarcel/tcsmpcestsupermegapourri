package fr.umlv.tcsmp.tcp;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;

public class KeyAttachment {
	private final ByteBuffer byteBuffer;
	private final Protocol protocol;
	private Response currentResponse;
	
	public KeyAttachment(ByteBuffer byteBuffer, Protocol protocol) {
		this.byteBuffer = byteBuffer;
		this.protocol = protocol;
	}
	
	public KeyAttachment(KeyAttachment keyAttachment) {
		this.byteBuffer = keyAttachment.getByteBuffer().duplicate();
		this.protocol = keyAttachment.getProtocol();
		this.currentResponse = keyAttachment.currentResponse;
	}
	
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
	
	public void setCurrentResponse(Response currentResponse) {
		this.currentResponse = currentResponse;
	}
	
	public Response getCurrentResponse() {
		return currentResponse;
	}
	
}
