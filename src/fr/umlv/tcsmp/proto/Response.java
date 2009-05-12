package fr.umlv.tcsmp.proto;

import java.nio.ByteBuffer;

/**
 * Class telling what actions the proto. must take. 
 */
public class Response {
	
	private final ByteBuffer response;
	private final String dest;
	
	private final ResponseAction action;
	
	public Response(ByteBuffer bb) {
		this(bb, null, ResponseAction.REPLY);
	}
	
	public Response(ResponseAction action) {
		this(null, null, action);
	}
	
	public Response(ByteBuffer bb, String dest) {
		this(bb, dest, ResponseAction.RELAY);
	}
	
	public Response(ByteBuffer bb, ResponseAction action) {
		this(bb, null, action);
	}
	
	public Response(ByteBuffer bb, String dest, ResponseAction action) {
		this.action = action;
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
	
	public ResponseAction getAction() {
		return action;
	}
}
