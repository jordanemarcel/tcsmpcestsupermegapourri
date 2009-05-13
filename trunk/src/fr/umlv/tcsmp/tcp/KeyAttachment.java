package fr.umlv.tcsmp.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

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
