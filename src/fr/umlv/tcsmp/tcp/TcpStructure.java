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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;
import fr.umlv.tcsmp.utils.TCSMPParser;

/**
 * This class is a generic TCP Client/Server. It is given a Protocol object which determines
 * if the TCP structure behaves like a client or server. It uses a Selector.
 * Every time the TCP session reads or writes on the network, it then asks to the Protocol object
 * what its next action should be.
 */
public class TcpStructure {
	/** Default TCP Application layer buffer size */
	private static int BUFFER_SIZE = 1024;
	/** Default timeout for the selector */
	private static long SELECTOR_TIMEOUT = 30000;
	/** Selector of the TCP structure */
	private final Selector selector;
	/** DNS Resolver of the TCP structure */
	private final DNSResolver dnsResolver;
	/** Protocol given to the TCP structure */
	private Protocol givenProtocol;
	//TODO javadoc
	private final ArrayList<PendingConnection> pendingConnectionList = 
		new ArrayList<PendingConnection>();
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
		TcpStructure.SELECTOR_TIMEOUT = 1000;
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
		KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, givenProtocol);
		Response response = givenProtocol.doIt(byteBuffer);
		keyAttachment.setCurrentResponse(response);
		protocolDomainMap.put(givenProtocol, new SocketData(null));
		String address = givenProtocol.getDefaultRelay();
		InetAddress inet = InetAddress.getByName(address);
		this.connectNewClient(inet, keyAttachment);
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
		while(selector.keys().size()>0 || givenProtocol.getProtocolMode()==ProtocolMode.SERVER) {
			this.closeTimedOutPendingConnection();
			selector.select(SELECTOR_TIMEOUT);
			this.checkTimeOut();
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
		System.out.println("FINISH");
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
				System.out.println("* TcpStructure: READ/WRITE");
				if(responseAction==ResponseAction.READ) {
					System.out.println("Next: READ");
				} else {
					System.out.println("Next: WRITE");
				}try {
					socketChannel.close();
				} catch (IOException e1) {
					System.err.println(e);
				}
				if(domain==null) {
					System.out.println("* TcpStructure: Domain null");
					if(socketChannel==originalClient) {
						System.out.println("* TcpStructure: Original client");
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						System.out.println("* TcpStructure: Not original client");
						System.out.println("* TcpStructure: cancelling key...");
						key.cancel();
						originalClient.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
						return;
					}
				} else {
					System.out.println("* TcpStructure: Domain non null - " + domain);
					SocketChannel client = socketData.getSocket(domain);
					if(socketChannel==client) {
						System.out.println("* TcpStructure: Socket is last client");
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						System.out.println("* TcpStructure: Socket is not last client");
						if(client==null) {
							System.out.println("* TcpStructure: Client is null");
							keyAttachment.setCurrentResponse(response);
							InetAddress domainAddress = dnsResolver.resolv(domain);
							this.connectNewClient(domainAddress, keyAttachment);
							key.cancel();
							System.out.println("* TcpStructure: cancelling key...");
							return;
						} else {
							System.out.println("* TcpStructure: Client is not null");
							client.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
							key.cancel();
							System.out.println("* TcpStructure: cancelling key...");
							return;
						}
					}
				}
			case CONTINUEREAD:
				System.out.println("* TcpStructure: CONTINUEREAD");
				key.interestOps(SelectionKey.OP_READ);
				return;
			case RELAYALL:
				System.out.println("* TcpStructure: RELAYALL");
				Collection<SocketChannel> allClient = protocolDomainMap.get(protocol).getClients();
				for(SocketChannel client: allClient) {
					if(client==socketChannel) {
						System.out.println("* TcpStructure: client is socketchannel");
						key.interestOps(SelectionKey.OP_WRITE);
					} else {
						System.out.println("* TcpStructure: client is not socketchannel");
						client.register(selector, TcpStructure.getResponseOps(responseAction), new KeyAttachment(keyAttachment));
					}
				}
				if(socketChannel==socketData.getOriginalClient()) {
					System.out.println("* TcpStructure: Socket is original client");
					System.out.println("* TcpStructure: cancelling key...");
					key.cancel();
				}
				return;
			case CLOSE:
				System.out.println("* TcpStructure: Close socket");
				key.cancel();
				socketChannel.close();
				return;
			}
		} catch (ClosedChannelException e) {
			System.err.println(e);
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		} catch (UnknownHostException e) {
			System.err.println(e);
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		} catch (IOException e) {
			System.err.println(e);
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
			try {
				socketChannel.close();
			} catch (IOException e1) {
				System.err.println(e);
			}
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
	private void connectNewClient(InetAddress address, KeyAttachment keyAttachment) throws IOException {
		Protocol protocol = keyAttachment.getProtocol();
		try {
			InetSocketAddress remoteIsa = new InetSocketAddress(address, protocol.getProtocolPort());
			SocketChannel client = SocketChannel.open();
			Response currentResponse = keyAttachment.getCurrentResponse();
			client.configureBlocking(false);
			SocketData socketData = protocolDomainMap.get(keyAttachment.getProtocol());
			if(socketData.getOriginalClient()==null) {
				socketData.setOriginalClient(client);
			}
			if(client.connect(remoteIsa)) {
				System.out.println("* TcpStructure: Immediate connection");
				socketData.putSocket(client, keyAttachment.getCurrentResponse().getDest());
				client.register(selector, TcpStructure.getResponseOps(currentResponse.getAction()), keyAttachment);
			} else {
				System.out.println("* TcpStructure: Non Immediate connection");
				SelectionKey selectionKey = client.register(selector, SelectionKey.OP_CONNECT, keyAttachment);
				PendingConnection pendingConnection = new PendingConnection(selectionKey);
				pendingConnectionList.add(pendingConnection);
			}
		} catch(IllegalArgumentException iae) {
			System.err.println(iae);
			System.err.println("* TcpStructure: Could not connect to the client");
			throw new IOException("* TcpStructure: Can't establish a connection with the client :-(");
		} catch (IOException e) {
			System.err.println(e);
			System.err.println("* TcpStructure: Could not connect to the client");
			throw new IOException("* TcpStructure: Can't establish a connection with the client :-(");
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
			System.out.println(TCSMPParser.decode(byteBuffer));
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			try {
				socketChannel.close();
			} catch (IOException e1) {
				System.err.println(e1);
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
			System.out.println(TCSMPParser.decode(byteBuffer));
			while(byteBuffer.hasRemaining()) {
				socketChannel.write(byteBuffer);
			}
			byteBuffer.clear();
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			try {
				socketChannel.close();
			} catch (IOException e1) {
				System.err.println(e1);
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
		System.out.println("* TcpStructure: Preparing to connect..");
		try {
			if(socketChannel.finishConnect()==false) {
				System.out.println("* TcpStructure: Closing!");
				socketChannel.close();
				return;
			}
			this.removePendingConnection(key);
			System.out.println("* TcpStructure: Connected!");
			SocketData socketData = protocolDomainMap.get(keyAttachment.getProtocol());
			socketData.putSocket(socketChannel, keyAttachment.getCurrentResponse().getDest());
			key.interestOps(TcpStructure.getResponseOps(keyAttachment.getCurrentResponse().getAction()));
		} catch (IOException e) {
			System.out.println(e);
			try {
				socketChannel.close();
			} catch (IOException e1) {
				System.err.println(e1);
			}
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			Protocol protocol = keyAttachment.getProtocol();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}
	
	//TODO javadoc
	private void closeTimedOutPendingConnection() {
		for(PendingConnection pendingConnection: pendingConnectionList) {
			if(pendingConnection.isTimedOut()) {
				System.out.println(pendingConnectionList.size());
				System.out.println("TIME OUT!!!!");
				System.out.println(pendingConnectionList.size());
				SelectionKey selectionKey = pendingConnection.getSelectionKey();
				KeyAttachment keyAttachment = (KeyAttachment)selectionKey.attachment();
				ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
				Protocol protocol = keyAttachment.getProtocol();
				byteBuffer.clear();
				Response cancelResponse = protocol.cancel(byteBuffer);
				this.handleResponse(selectionKey, cancelResponse);
			}
		}
	}
	
	//TODO javadoc
	private void removePendingConnection(SelectionKey selectionKey) {
		Iterator<PendingConnection> it = pendingConnectionList.iterator();
		while(it.hasNext()) {
			PendingConnection pendingConnection = it.next();
			if(pendingConnection.getSelectionKey()==selectionKey) {
				it.remove();
			}
		}
//		for( PendingConnection pendingConnection: pendingConnectionList) {
//			if(pendingConnection.getSelectionKey()==selectionKey) {
//				pendingConnectionList.remove(pendingConnection);
//				//remove = true;
//				//System.out.println("Key removed!");
//				//break;
//			}
//		}
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
