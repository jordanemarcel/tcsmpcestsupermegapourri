package fr.umlv.tcsmp.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TCSMPLogger {
	
	private final static Logger log = Logger.getLogger("fr.umlv.tcsmp");
	
	public static void debug(String msg) {
		log.log(Level.INFO, msg);
	}

	public static void error(String msg) {
		log.log(Level.SEVERE, msg);
	}
}
