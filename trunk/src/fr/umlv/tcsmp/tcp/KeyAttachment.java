package fr.umlv.tcsmp.tcp;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;

/**
 * This class represents the object that is attached to a selector key.
 * It contains necessary information for the current selected TCP channel.
 */
public class KeyAttachment {
	/** The ByteBuffer associated with a SocketChannel */
	private final ByteBuffer byteBuffer;
	/** The protocol associated with a SocketChannel, one per session */
	private final Protocol protocol;
	/** The last response that was received */
	private Response currentResponse;
	
	/**
	 * Creates the KeyAttachment object with the given ByteBuffer and Protocol.
	 * @param byteBuffer - the given ByteBuffer
	 * @param protocol - the given Protocol
	 */
	public KeyAttachment(ByteBuffer byteBuffer, Protocol protocol) {
		this.byteBuffer = byteBuffer;
		this.protocol = protocol;
	}
	
	/**
	 * Creates a copy of the given KeyAttachement, only duplicating its ByteBuffer.
	 * @param keyAttachment - the KeyAttachement to copy
	 */
	public KeyAttachment(KeyAttachment keyAttachment) {
		this.byteBuffer = keyAttachment.getByteBuffer().duplicate();
		this.protocol = keyAttachment.getProtocol();
		this.currentResponse = keyAttachment.currentResponse;
	}
	
	/**
	 * Returns the ByteBuffer
	 * @return - the ByteBuffer
	 */
	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}
	
	/**
	 * Returns the Protocol
	 * @return - the Protocol
	 */
	public Protocol getProtocol() {
		return protocol;
	}
	
	/**
	 * Sets the last Response
	 * @param currentResponse - the last Response
	 */
	public void setCurrentResponse(Response currentResponse) {
		this.currentResponse = currentResponse;
	}
	
	/**
	 * Returns the last Response
	 * @return - the last Response
	 */
	public Response getCurrentResponse() {
		return currentResponse;
	}
	
}
