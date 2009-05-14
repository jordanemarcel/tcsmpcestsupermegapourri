package fr.umlv.tcsmp.states;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.server.BannerServerState;

/**
 * Class representing a state in the TCSMP proto. 
 */
public abstract class TCSMPState {
	
	public static final int TIMEOUT_NONE = 0;
	public static final int TIMEOUT_WRITE = 1;
	public static final int TIMEOUT_CLOSE = 2;
	
	private final Timer timeoutTimer;
	private int timeoutState;
	private int timeoutTime;
	private final TimerTask timeoutTask;
	
	public TCSMPState() {
		timeoutTimer = new Timer();
		timeoutTime = 0;
		timeoutState = TIMEOUT_NONE;
		timeoutTask = new TimerTask() {
			@Override
			public void run() {
				timeoutState = TIMEOUT_WRITE;
			}
		};
	}
	
	public TCSMPState(int tout) {
		this();
		timeoutTimer.schedule(timeoutTask, tout);
		timeoutTime = tout;
	}
	
	/**
	 * Rearm the timeout
	 */
	public void timeoutRearm() {
		timeoutTimer.cancel();
		timeoutTimer.schedule(timeoutTask, timeoutTime);
	}
	
	/**
	 * @return timeout state
	 */
	public int getTimeoutState() {
		return timeoutState;
	}
	
	/**
	 * @param timeout state timeoutState to set
	 */
	public void setTimeoutState(int timeoutState) {
		this.timeoutState = timeoutState;
	}
	
	/**
	 * is Timeout ?
	 */
	public boolean isTimeout() {
		return timeoutState != TIMEOUT_NONE;
	}
	
	/**
	 * Parse TCSMP command found in the ByteBuffer and
	 * return a Response englobbing several useful things
	 * for the proto. handler.
	 */
	public abstract Response processCommand(Protocol proto, ByteBuffer bb);
	
	/**
	 * Return a new default state for a server.
	 */
	public static TCSMPState newDefaultServerState() {
		return new BannerServerState();
	}

	/**
	 * Return a new default state for a client 
	 */
	public static TCSMPState newDefaultClientState() {
		return new BannerClientState();
	}
	
	
}
