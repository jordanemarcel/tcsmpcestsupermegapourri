package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.TCSMPCommandParser;
import fr.umlv.tcsmp.states.TCSMPState;

public class TeloState implements TCSMPState {

	private static String banner = "Hello I'm the TCSMP server. Nice to meet you.";
	
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		
		String [] args = TCSMPCommandParser.parse(bb);
		
		if (args.length == 0 || args[0].equals("TELO") == false) {
			System.out.println("fook");
			return null;
		}

		/**
		 * XXX: Comment va-t-on faire pour changer de state ici ?
		 * Faut passer le protocole en param ?
		 */
		
		
		/**
		 * Ici on va créer un Response avec le code 200 et un welcome message.
		 * XXX: On nique le bb qu'on a passé en param et on crée un Response qui
		 * va bien ?
		 */
		
		/**
		 * Change state
		 */
		proto.setState(new FromState());
		
		/**
		 * Create response buffer.
		 */
		ByteBuffer response = ByteBuffer.wrap(new String("200-TCSMPv1\r\n200 OK " + banner + "\r\n").getBytes());
		
		System.out.println("ok");
		return new Response(response);
	}
}