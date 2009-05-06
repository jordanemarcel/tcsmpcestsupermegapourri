package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

/**
 * Class telling what actions the proto. must take. 
 */
public class Response {
	
	private final ByteBuffer response;
	private final String dest;
	
	public Response(ByteBuffer bb) {
		this(bb, null);
	}
	
	public Response(ByteBuffer bb, String dest) {
		this.response = bb;
		this.dest = dest;
	}
	
	public ByteBuffer getResponse() {
		return response;
	}
	
	public boolean hasDest() {
		return dest != null;
	}
	
	public String getDest() {
		return dest;
	}
}
