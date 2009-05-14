package fr.umlv.tcsmp.states;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.client.BannerClientState;
import fr.umlv.tcsmp.states.server.BannerServerState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * Class representing a state in the TCSMP proto. 
 */
public abstract class TCSMPState {

	public static final int TIMEOUT_NONE = 0;
	public static final int TIMEOUT_WRITE = 1;
	public static final int TIMEOUT_CLOSE = 2;

	private int timeoutState;
	private int timeoutTime;
	private Timer currentTimer;

	public TCSMPState() {
		timeoutTime = 0;
		timeoutState = TIMEOUT_NONE;
	}

	public TCSMPState(int tout) {
		this();
		currentTimer = createTimer(createTimerTask(), tout);
		timeoutTime = tout;
	}


	/**
	 * Return a response corresponding to the timeout state
	 */
	protected Response timeoutResponse(ByteBuffer bb) {
		if (getTimeoutState() == TIMEOUT_WRITE) {
			bb.clear();
			bb.put(ErrorReplies.timeoutError());
			bb.flip();
			setTimeoutState(TIMEOUT_CLOSE);
			return new Response(ResponseAction.WRITE);
		}

		if (getTimeoutState() == TIMEOUT_CLOSE) {
			return new Response(ResponseAction.CLOSE);
		}
		
		throw new AssertionError("Invalid timeout state in timeoutResponse() call");
	}


	private static Timer createTimer(TimerTask task, int tout) {
		Timer t = new Timer();
		t.schedule(task, tout);
		return t;
	}
	
	/**
	 * Rearm the timeout
	 */
	public void timeoutReset() {
		if (timeoutTime == 0)
			return;
		
		try {
			currentTimer.cancel();
			currentTimer = createTimer(createTimerTask(), timeoutTime);
		}
		catch (Exception e) {
		}
	}
	
	/**
	 * Clear the timeout.
	 */
	public void timeoutClear() {
		if (timeoutTime == 0)
			return;
		
		try {
			currentTimer.cancel();
			currentTimer.purge();
		}
		catch (Exception e) {
		}
	}
	
	private TimerTask createTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				timeoutState = TIMEOUT_WRITE;
			}
		};
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
