package fr.umlv.tcsmp.states.client;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.states.TCSMPState;
import fr.umlv.tcsmp.utils.TCSMPParser;

public class MailClientState extends TCSMPState {

	private ResponseAction resp = null;
	private PkeyClientState pkeyState = null;
	private boolean sentRequest = false;

	private ByteBuffer encodedMail = null;
	private int offset = 0;

	public MailClientState() {
	}

	public MailClientState(PkeyClientState pkeyState) {
		this.pkeyState = pkeyState;
	}

	public Response processCommand(Protocol proto, ByteBuffer bb) {
		if (encodedMail == null) {
			encodedMail = TCSMPParser.encode(proto.getMail());
		}
		if (!sentRequest) {
			// MAIL Request was not send yet
			if (resp == null) {
				// Mail has not yet been sent
				bb.put(TCSMPParser.encode("MAIL\r\n"));
				bb.flip();
				resp = ResponseAction.WRITE;

				return new Response(resp);
			}

			if (resp == ResponseAction.WRITE) {
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
					resp = null;
					break;
				case 451:
				case 554:
				case 503:
				default:
					proto.setState(new QuitClientState());
					proto.addMainError(list.get(0) + " " + list.get(1));
				}
				bb.clear();
				return proto.doIt(bb);
			}
		}
		else {
			// MAIL request was sent
			if (resp == null) {
				// Mail has not yet been sent
				bb.put(encodedMail);
				offset += bb.limit();
				bb.flip();
				if (0 == encodedMail.remaining()) {
					// Data was sent, signify we want to get the reply
					resp = ResponseAction.REPLY;
				}

				return new Response(resp);
			}

			if (resp == ResponseAction.REPLY) {
				resp = ResponseAction.READ;
				return new Response(resp);
			}

			if (resp == ResponseAction.READ) {
				// We got here because we got the answer
				ArrayList<String> list = new ArrayList<String>();
				TCSMPParser.parseAnswer(bb, list);
				switch(Integer.parseInt(list.get(0))) {
				// States
				case 250:
					sentRequest = true;
					resp = ResponseAction.REPLY;
					if (pkeyState != null) 
						proto.setState(pkeyState);	
					else
						proto.setState(new PkeyClientState());
					break;
				case 451:
				case 503:
				case 554:
				default:
					proto.setState(new QuitClientState());
					proto.addMainError(list.get(0) + " " + list.get(1));
				}
				bb.clear();
				return proto.doIt(bb);
			}
		}

		return null;
	}
	
	@Override
	public Response cancel(Protocol proto, ByteBuffer bb) {
		bb.clear();
		if (!sentRequest) {
			if (resp == ResponseAction.WRITE) {
				proto.addMainError("Communication error while MAIL'ing.");
			}
			else {
				proto.addMainError("Communication error while getting MAIL response.");
			}
		}
		else {
			if (resp == ResponseAction.WRITE) {
				proto.addMainError("Communication error while sending mail data.");
			}
			else {
				proto.addMainError("Communication error while response for the mailed data.");
			}
		}
		return new Response(ResponseAction.CLOSE);
	}
}
