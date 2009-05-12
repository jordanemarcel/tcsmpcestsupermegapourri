package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.puzzle.Puzzle;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.ErrorReplies;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class MailClientState extends TCSMPState {

	private ResponseAction resp = null;
	private PkeyClientState pkeyState = null;
	private boolean sentRequest = false;

	private byte[] encodedMail = null;
	private int offset = 0;

	public MailClientState() {
	}

	public MailClientState(PkeyClientState pkeyState) {
		this.pkeyState = pkeyState;
	}

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (encodedMail == null) {
			encodedMail = TCSMPParser.encode(proto.getMail().toString());
		}

		if (!sentRequest) {
			// MAIL Request was not send yet
			if (resp == null) {
				// Mail has not yet been sent
				bb.clear();
				bb.put(TCSMPParser.encode("MAIL\r\n"));
				bb.flip();
				resp = ResponseAction.REPLY;

				return new Response(resp);
			}

			if (resp == ResponseAction.REPLY) {
				// Request was sent, signify we want to get the reply
				resp = ResponseAction.READ;
				return new Response(resp);
			}

			if (resp == ResponseAction.READ) {
				// We got here because we got the answer
				ArrayList<String> list = new ArrayList<String>();
				TCSMPParser.parseAnswer(bb, list);
				switch(Integer.parseInt(list.get(0))) {
				// States
				case 354:
					sentRequest = true;
					resp = ResponseAction.REPLY;
					proto.setState(new ApzlClientState());	
					return proto.doIt(bb);
				default:
					throw new AssertionError("Pouet");
				}
			}
		}
		else {
			// MAIL request was sent
			if (resp == null) {
				// Mail has not yet been sent
				bb.clear();
				bb.put(encodedMail, offset, encodedMail.length - offset);
				offset += bb.limit();
				bb.flip();
				resp = ResponseAction.REPLY;

				return new Response(resp);
			}

			if (resp == ResponseAction.REPLY) {
				if (offset == encodedMail.length) {
					// Data was sent, signify we want to get the reply
					resp = ResponseAction.READ;
				}
				return new Response(resp);
			}

			if (resp == ResponseAction.READ) {
				// We got here because we got the answer
				ArrayList<String> list = new ArrayList<String>();
				TCSMPParser.parseAnswer(bb, list);
				switch(Integer.parseInt(list.get(0))) {
				// States
				case 354:
					sentRequest = true;
					resp = ResponseAction.REPLY;
					if (pkeyState != null) 
						proto.setState(pkeyState);	
					else
						proto.setState(new PkeyClientState());
					return proto.doIt(bb);
				default:
					throw new AssertionError("Pouet");
				}
			}
		}

		return null;
	}
}
