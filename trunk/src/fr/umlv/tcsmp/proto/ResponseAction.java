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
	/* tell to the TCP structure that the socket must be in READ state */
	READ,
	/* tell to the TCP structure must be in the WRITE state */
	WRITE,
	/* continue reading */
	CONTINUEREAD,
}
