package fr.umlv.tcsmp.proto;


/**
 * Class telling what actions the proto. must take. 
 */
public class Response {
	
	private final String dest;
	
	private final ResponseAction action;
	
	public Response() {
		this(null, ResponseAction.WRITE);
	}
	
	public Response(ResponseAction action) {
		this(null, action);
	}
	
	public Response(String dest) {
		this(dest, ResponseAction.WRITE);
	}
	
	public Response(String dest, ResponseAction action) {
		this.action = action;
		this.dest = dest;
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
	
	@Override
	public String toString() {
		return "action: " + action + " - dest: " + dest;
	}
}
