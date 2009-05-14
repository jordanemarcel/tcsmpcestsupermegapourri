package fr.umlv.tcsmp.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;

/**
 * This class is a generic TCP Client/Server. It is given a Protocol object which determines
 * if the TCP structure behaves like a client or server. It uses a Selector.
 * Every time the TCP session reads or writes on the network, it then asks to the Protocol object
 * what its next action should be.
 */
public class TcpStructure {
	/** Default TCP Application layer buffer size */
	public static int BUFFER_SIZE = 1024;
	/** Default timeout for the selector */
	public static long SELECTOR_TIMEOUT = 30000;
	/** Selector of the TCP structure */
	private final Selector selector;
	/** DNS Resolver of the TCP structure */
	private final DNSResolver dnsResolver;
	/** Protocol given to the TCP structure */
	private Protocol givenProtocol;
	/** Map that associates a Protocol (session) with a SocketData (list of connected
	 *  clients) */
	private final HashMap<Protocol, SocketData> protocolDomainMap = 
		new HashMap<Protocol, SocketData>();

	/**
	 * Builds a new TCP structure with a given TCSMP Resolver
	 * @param dnsResolver - A DNS Resolver
	 * @throws IOException - if the selector can't be open
	 */
	public TcpStructure(DNSResolver dnsResolver) throws IOException {
		this.selector = Selector.open();
		this.dnsResolver = dnsResolver;
	}

	/**
	 * Launches the TCP structure with the given protocol. The protocol should indicates
	 * if it acts as a client or as a server.
	 * @param protocol - the protocol that handles the Application layer of the TCP Structure
	 * @throws IOException - if something goes wrong
	 */
	public void processProtocol(Protocol protocol) throws IOException {
		this.givenProtocol = protocol;
		switch (protocol.getProtocolMode()) {
		case CLIENT:
			System.out.println("* TcpStructure: CLIENT Mode");
			this.startClient(protocol.getProtocolPort());
			break;
		case SERVER:
			System.out.println("* TcpStructure: SERVER Mode");
			this.startServer(protocol.getProtocolPort());
			break;
		}
	}
	
	/**
	 * Starts the structure in Client mode.
	 * @param port - port the client is going to connect on
	 * @throws IOException - if the connection goes wrong
	 */
	private void startClient(int port) throws IOException {
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
		KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, givenProtocol);
		Response response = givenProtocol.doIt(byteBuffer);
		keyAttachment.setCurrentResponse(response);
		String domain = response.getDest();
		this.connectNewClient(domain, keyAttachment);
		this.handleSelector();
		System.out.println("* TcpStructure: End of Transmission");
	}

	/**
	 * Starts the structure in Server mode.
	 * @param port - port that the server opens
	 * @throws IOException - if the server goes wrong
	 */
	private void startServer(int port) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress localIsa = new InetSocketAddress(port);
		serverSocketChannel.socket().bind(localIsa);
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.handleSelector();
	}
	
	/**
	 * Starts the selector. If the TCP Structure is in Client mode, stops when
	 * no more socket is connected. In Server mode, loop infinitely.
	 * @throws IOException - if the selector goes wrong
	 */
	private void handleSelector() throws IOException {
		int nbKeysSelected;
		while(selector.keys().size()>0 || givenProtocol.getProtocolMode()==ProtocolMode.SERVER) {
			do {
				nbKeysSelected = selector.select(SELECTOR_TIMEOUT);
				this.checkTimeOut();
			} while(nbKeysSelected<1);
			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			for(SelectionKey key: selectionKeys) {
				if(key.isValid() && key.isAcceptable()) {
					this.doAccept(key);
				}
				if(key.isValid() && key.isConnectable()) {
					this.doConnect(key);
				}
				if(key.isValid() && key.isReadable()) {
					this.doRead(key);
				}
				if(key.isValid() && key.isWritable()) {
					this.doWrite(key);
				}
			}
			selectionKeys.clear();
		}
	}
	
	/**
	 * This method checks if the keys of the selector that have not been selected
	 * are timed out. If so, it calls the doIt() method of the key's protocol.
	 */
	private void checkTimeOut() {
		Set<SelectionKey> selectionKeys = selector.keys();
		Set<SelectionKey> selectedSelectionKeys = selector.selectedKeys();
		for(SelectionKey selectionKey: selectionKeys) {
			if(!selectedSelectionKeys.contains(selectionKey)) {
				KeyAttachment keyAttachment = (KeyAttachment)selectionKey.attachment();
				if(keyAttachment==null) {
					continue;
				}
				Protocol protocol = keyAttachment.getProtocol();
				ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
				if(protocol.isTimeout()) {
					Response response = protocol.doIt(byteBuffer);
					keyAttachment.setCurrentResponse(response);
					this.handleResponse(selectionKey, response);
				}
			}
		}
	}

	/**
	 * Response handler. Each time the TCP structure writes or reads on the network, it asks
	 * thereafter the protocol what it is going to do. The answer impacts on the selector
	 * register system, the creation or closure of TCP connection.
	 * @param key - The selected key associated with the response
	 * @param response - The response given by the protocol
	 */
	private void handleResponse(SelectionKey key, Response response) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		Protocol protocol = keyAttachment.getProtocol();
		SocketData socketData = protocolDomainMap.get(protocol);
		String domain = response.getDest();
		SocketChannel originalClient = socketData.getOriginalClient();
		ResponseAction responseAction = response.getAction();
		try {
			switch (responseAction) {
			case READ: case WRITE:
				if(domain==null) {
					if(socketChannel==originalClient) {
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						key.cancel();
						originalClient.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
						return;
					}
				} else {
					SocketChannel client = socketData.getSocket(domain);
					if(socketChannel==client) {
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						key.cancel();
						if(client==null) {
							keyAttachment.setCurrentResponse(response);
							this.connectNewClient(domain, keyAttachment);
							return;
						} else {
							client.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
							return;
						}
					}
				}
			case RELAYALL:
				Collection<SocketChannel> allClient = protocolDomainMap.get(protocol).getClients();
				for(SocketChannel client: allClient) {
					if(client==socketChannel) {
						key.interestOps(SelectionKey.OP_WRITE);
					} else {
						client.register(selector, TcpStructure.getResponseOps(responseAction), new KeyAttachment(keyAttachment));
					}
				}
				if(socketChannel==socketData.getOriginalClient()) {
					key.cancel();
				}
				return;
			case CLOSE:
				socketChannel.close();
				return;
			}
		} catch (ClosedChannelException e) {
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
			System.err.println(e);
		} catch (IOException e) {
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
			System.err.println(e);
			if(socketChannel.isConnected()) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println(e);
				}
			}
			System.err.println(e);
		}
	}

	/**
	 * First, this method resolves the domain using the DNS Resolver and gets an address
	 * of a server. Then, it connects this new server and register correctly with the selector.
	 * @param domain - the domain that is going to be resolved to get the address of a server
	 * @param keyAttachment - The keyAttachment that contains information about the session protocol,
	 * the ByteBuffer and the last response given by the protocol.
	 * @throws IOException
	 */
	private void connectNewClient(String domain, KeyAttachment keyAttachment) throws IOException {
		Protocol protocol = keyAttachment.getProtocol();
		if(domain==null) {
			throw new IOException("Can't establish a connection with a null client :-(");
		}
		try {
			InetAddress domainAddress = dnsResolver.resolv(domain);
			InetSocketAddress remoteIsa = new InetSocketAddress(domainAddress, protocol.getProtocolPort());
			SocketChannel client = SocketChannel.open();
			Response currentResponse = keyAttachment.getCurrentResponse();
			client.configureBlocking(false);
			if(client.connect(remoteIsa)) {
				client.register(selector, TcpStructure.getResponseOps(currentResponse.getAction()), keyAttachment);
			} else {
				client.register(selector, SelectionKey.OP_CONNECT, keyAttachment);
			}
		} catch(IllegalArgumentException iae) {
			System.err.println(iae);
			System.err.println("Could not connect to the client");
			throw new IOException("Can't establish a connection with the client :-(");
		} catch (UnknownHostException e) {
			System.err.println(e);
			System.err.println("Could not connect to the client");
			throw new IOException("Can't establish a connection with the client :-(");
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("Could not connect to the client");
			throw new IOException("Can't establish a connection with the client :-(");
		}
	}

	/**
	 * Selector method: accepts a new connection
	 * @param key - selected key
	 */
	private void doAccept(SelectionKey key) {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
		Protocol newServerProtocol = givenProtocol.newProtocol();
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			System.out.println("* TcpStructure: New connection from: " + socketChannel.socket().getRemoteSocketAddress());
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
			KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, newServerProtocol);
			SocketData socketData = new SocketData(socketChannel);
			protocolDomainMap.put(newServerProtocol, socketData);
			socketChannel.configureBlocking(false);
			Response response = newServerProtocol.doIt(byteBuffer);
			ResponseAction responseAction = response.getAction();
			switch (responseAction) {
			case READ: case WRITE: 
				socketChannel.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
				return;
			case CLOSE:
				socketChannel.close();
				return;
			}
		} catch (IOException e) {
			System.err.println("Could not accept a new connection");
			System.err.println(e);
			return;
		}
	}

	/**
	 * Selector method: read from a socket
	 * @param key - selected key
	 */
	private void doRead(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
		Protocol protocol = keyAttachment.getProtocol();
		System.out.println("* TcpStructure: Reading from " + socketChannel.socket().getRemoteSocketAddress());
		try {
			socketChannel.read(byteBuffer);
			byteBuffer.flip();
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			if(socketChannel.isConnected()) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println(e1);
				}
			}
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}

	/**
	 * Selector method: writes to a socket
	 * @param key - selected key
	 */
	private void doWrite(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
		Protocol protocol = keyAttachment.getProtocol();
		System.out.println("* TcpStructure: Writing to " + socketChannel.socket().getRemoteSocketAddress());
		try {
			while(byteBuffer.hasRemaining()) {
				socketChannel.write(byteBuffer);
			}
			byteBuffer.clear();
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			if(socketChannel.isConnected()) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println(e1);
				}
			}
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}

	/**
	 * Selector method: connects to a socket
	 * @param key - selected key
	 */
	private void doConnect(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		System.out.println("* TcpStructure: Connecting to " + socketChannel.socket().getRemoteSocketAddress());
		try {
			if(socketChannel.finishConnect()) {
				socketChannel.close();
			}
			key.interestOps(TcpStructure.getResponseOps(keyAttachment.getCurrentResponse().getAction()));
		} catch (IOException e) {
			System.out.println(e);
			if(socketChannel.isConnected()) {
				try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println(e1);
				}
			}
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			Protocol protocol = keyAttachment.getProtocol();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}

	/**
	 * Static method that returns the interesting Operation for a SocketChannel that wants to register
	 *  with a selector, corresponding to the given ResponseAction
	 * @param responseAction - the given ResponseAction
	 * @return - The interesting Operation for the socket that wants to register with the selector
	 */
	private static int getResponseOps(ResponseAction responseAction) {
		switch (responseAction) {
		case READ:
			return SelectionKey.OP_READ;
		case WRITE:
			return SelectionKey.OP_WRITE;
		case RELAYALL:
			return SelectionKey.OP_WRITE;
		default:
			throw new IllegalArgumentException("No Ops for " + responseAction.name());
		}
	}

}
