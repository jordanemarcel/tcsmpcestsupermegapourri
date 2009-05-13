package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;
import java.util.Scanner;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class DataServerState extends TCSMPState {

	private boolean send = false;
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
	
		if (send) {
			proto.setState(new PkeyServerState());
			return new Response(ResponseAction.READ);
		}
		
		Scanner sc = new Scanner(new String(bb.array()));
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			proto.mail(line + "\r\n");
			if (line.equals(".")) {
				bb.clear();
				bb.put(TCSMPParser.encode("250 OK\r\n"));
				bb.flip();
				send = true;
				return new Response(ResponseAction.WRITE);
			}
		}
		
		/* no data to send, read more */
		return new Response(ResponseAction.READ);
	}
}
