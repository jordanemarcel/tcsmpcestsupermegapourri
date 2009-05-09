package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.util.Scanner;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.states.TCSMPState;

public class DataState implements TCSMPState {

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		/*
		 * A fuking state \o/
		 * Append data in string builder inside the proto ?
		 */
		
		Scanner sc = new Scanner(new String(bb.array()));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			proto.mail(line + "\r\n");
			if (line.equals(".")) {
				proto.setState(new PkeyState());
				ByteBuffer response = ByteBuffer.wrap("250 OK\r\n".getBytes());
				return new Response(response);
			}
		}
		
		/* no data to send, return null */
		return null;
	}
}
