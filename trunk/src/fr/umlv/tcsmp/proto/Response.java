package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

/**
 * Class telling what actions the proto. must take. 
 */
public class Response {
	
	private final ByteBuffer response;
	
	public Response(ByteBuffer bb) {
		this.response = bb;
	}
	
	public ByteBuffer getResponse() {
		return response;
	}
}
