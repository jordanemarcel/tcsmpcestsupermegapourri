package fr.umlv.tcsmp.proto;

public enum ResponseAction {
	
	/* relay the buffer to the dest server */
	RELAY,
	/* reply to the client */
	REPLY,
	/* close connection */
	CLOSE,
	/* relay the buffer to all dest servers */
	RELAYALL,
	
}
