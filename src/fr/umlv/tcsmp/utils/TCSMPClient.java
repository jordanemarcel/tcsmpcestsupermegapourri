package fr.umlv.tcsmp.utils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * A fucking TCSMPClient for testing purpose only.
 */

class Puzzle {
	private final String domain;
	private final String puzzle;
	private final String size;
	
	public Puzzle(String domain, String puzzle, String size) {
		this.domain = domain;
		this.puzzle = puzzle;
		this.size = size;
	}
	
	public String getPuzzle() {
		return puzzle;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public String getSize() {
		return size;
	}
	
	@Override
	public String toString() {
		return domain + " " + size + " " + puzzle;
	}
}

public class TCSMPClient {

	private static final int DEFAULT_PORT = 26;

	private final Socket tcsmpConn;
	private final BufferedReader in;
	private final BufferedWriter out;

	public TCSMPClient() throws UnknownHostException, IOException {
		this("127.0.0.1", DEFAULT_PORT);
	}

	public TCSMPClient(String host) throws UnknownHostException, IOException {
		this(host, DEFAULT_PORT);
	}

	public TCSMPClient(String host, int port) throws UnknownHostException, IOException {
		tcsmpConn = new Socket(InetAddress.getByName(host), port);
		
		in = new BufferedReader(new InputStreamReader(tcsmpConn.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(tcsmpConn.getOutputStream()));
		
		banner();
	}

	private void banner() throws IOException {
		String line;
		if ((line = in.readLine()).startsWith("220 ") == false) 
			throw new UnknownHostException("This host does not seem to be a TCSMP server (" + line + ")");
	}
	
	
	private void checkResponse() throws IOException {
		String line;
		if ((line = in.readLine()).startsWith("2") == false) 
			throw new IllegalStateException("ERROR: " + line);
		}

	public void telo(String domain) throws IOException {
		System.out.println("telo");
		out.write("TELO " + domain + "\r\n");
		out.flush();
		checkResponse();
	}
	
	public void from(String mail) throws IOException {
		out.write("FROM <" + mail + ">\r\n");
		out.flush();
		checkResponse();
	}
	
	public void rcpt(String rcpt) throws IOException {
		out.write("RCPT <" + rcpt + ">\r\n");
		out.flush();
		checkResponse();
	}
	
	public List<Puzzle> apzl() throws IOException {
		out.write("APZL\r\n");
		out.flush();
		List<Puzzle> pzls = new ArrayList<Puzzle>();
		while (true) {
			String line = in.readLine();
			String[] args = line.split("\\s+");
			if (args[0].startsWith("215-") == false) {
				if (args[0].equals("215") == false || args.length != 4)
					throw new IllegalStateException("ERROR:" + line);
				pzls.add(new Puzzle(args[1], args[3], args[2]));
				break;
			}
			else {
				if (args.length != 3)
					throw new IllegalStateException("ERROR:" + line);
				pzls.add(new Puzzle(args[0].substring(4), args[2], args[1]));
			}
		}
		return pzls;
	}
	
	public void mail() throws IOException {
		out.write("MAIL\r\n");
		out.flush();
		checkResponse();
		System.out.println("Start mail input; end with <CRLF>.<CRLF>");
		Scanner userin = new Scanner(System.in);
		while (userin.hasNextLine())
		{
			String line =  userin.nextLine();
			if (line.equals(".")) break;
			out.write(line + "\r\n");
		}
		out.write("\r\n.\r\n");
		out.flush();
		checkResponse();
	}
	
	public void pkey(Puzzle key) throws IOException {
		out.write("PKEY " + key + "\r\n");
		out.flush();
		checkResponse();
	}
	
	public void quit() throws IOException {
		out.write("QUIT\r\n");
		out.flush();
		checkResponse();
	}

	public void close() throws IOException {
		tcsmpConn.close();
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		TCSMPClient client = new TCSMPClient("127.0.0.1", 2626);
		client.telo("foo.be");
		client.from("gni@clem1.be");
		client.rcpt("bar@foo.be");
		List<Puzzle> pzls = client.apzl();
		for (Puzzle pzl : pzls)
			System.out.println("resolve this dude : " + pzl.getPuzzle());
		client.mail();
		client.pkey(pzls.get(0)); /* cheetah ! */
		client.quit();
		client.close();
	}
}