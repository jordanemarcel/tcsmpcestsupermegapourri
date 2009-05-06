package fr.umlv.tcsmp.states.server;

import java.nio.ByteBuffer;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.TCSMPCommandParser;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;

public class RctpState implements TCSMPState {

	@Override
	public Response processCommand(Protocol proto, ByteBuffer bb) {
		String [] args = TCSMPCommandParser.parse(bb);

		/**
		 * ICI on va s'amuser puisque on ne switch pas forcement de
		 * state.
		 * 
		 * Qu'est ce qu'on fait ?
		 * 
		 * 1 solution:
		 * 
		 * 	- on accept ici la commande APZL.
		 *  - si on lit un APZL, on passe le state en APZL
		 *  - on appelle processCommand du state APZL avec le bb qu'on a lu
		 *  
		 */
		if (args.length != 2 || args[0].equals("RCTP") == false) {
			return new Response(ErrorReplies.unknowCommand("RCTP", args[0]));
		}

		return null;
	}
}
