package fr.umlv.tcsmp.tcp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeoutException;

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
	/** Used for debug */
	private static boolean debug = true;
	/** Default TCP Application layer buffer size */
	public static int BUFFER_SIZE = 1024;
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
	 * @throws TimeoutException 
	 */
	public void processProtocol(Protocol protocol) throws IOException, TimeoutException {
		this.givenProtocol = protocol;
		switch (protocol.getProtocolMode()) {
		case CLIENT:
			this.debug("CLIENT Mode");
			this.startClient(protocol.getProtocolPort());
			break;
		case SERVER:
			this.debug("SERVER Mode");
			this.startServer(protocol.getProtocolPort());
			break;
		}
	}

	/**
	 * Starts the structure in Client mode.
	 * @param port - port the client is going to connect on
	 * @throws IOException - if the connection goes wrong
	 * @throws TimeoutException 
	 */
	private void startClient(int port) throws IOException, TimeoutException {
		TcpStructure.SELECTOR_TIMEOUT = 1000;
		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
		KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, givenProtocol);
		Response response = givenProtocol.doIt(byteBuffer);
		keyAttachment.setCurrentResponse(response);
		protocolDomainMap.put(givenProtocol, new SocketData(null));
		String address = givenProtocol.getDefaultRelay();
		this.debug(address);
		InetAddress inet = InetAddress.getByName(address);
		this.debug(inet);
		this.connectNewClient(inet, keyAttachment);
		this.handleSelector();
		this.debug("End of Transmission");
	}

	/**
	 * Starts the structure in Server mode.
	 * @param port - port that the server opens
	 * @throws IOException - if the server goes wrong
	 * @throws TimeoutException 
	 */
	private void startServer(int port) throws IOException, TimeoutException {
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
	 * @throws TimeoutException 
	 */
	private void handleSelector() throws IOException, TimeoutException {
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
		this.debug("FINISH");
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
				this.debug("READ/WRITE");
				if(responseAction==ResponseAction.READ) {
					this.debug("Next: READ");
				} else {
					this.debug("Next: WRITE");
				}
				if(domain==null) {
					this.debug("Domain null");
					if(socketChannel==originalClient) {
						this.debug("Original client");
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						this.debug("Not original client");
						this.debug("cancelling key...");
						key.cancel();
						originalClient.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
						return;
					}
				} else {
					this.debug("Domain non null - " + domain);
					SocketChannel client = socketData.getSocket(domain);
					if(socketChannel==client) {
						this.debug("Socket is last client");
						key.interestOps(TcpStructure.getResponseOps(responseAction));
						return;
					} else {
						this.debug("Socket is not last client");
						if(client==null) {
							this.debug("Client is null");
							keyAttachment.setCurrentResponse(response);
							InetAddress domainAddress = dnsResolver.resolv(domain);
							this.connectNewClient(domainAddress, keyAttachment);
							key.cancel();
							this.debug("cancelling key...");
							return;
						} else {
							this.debug("Client is not null");
							client.register(selector, TcpStructure.getResponseOps(responseAction), keyAttachment);
							key.cancel();
							this.debug("cancelling key...");
							return;
						}
					}
				}
			case CONTINUEREAD:
				this.debug("CONTINUEREAD");
				key.interestOps(SelectionKey.OP_READ);
				return;
//			case RELAYALL:
//				this.debug("RELAYALL");
//				Collection<SocketChannel> allClient = protocolDomainMap.get(protocol).getClients();
//				for(SocketChannel client: allClient) {
//					if(client==socketChannel) {
//						this.debug("client is socketchannel");
//						key.interestOps(SelectionKey.OP_WRITE);
//					} else {
//						this.debug("client is not socketchannel");
//						client.register(selector, TcpStructure.getResponseOps(responseAction), new KeyAttachment(keyAttachment));
//					}
//				}
//				if(socketChannel==socketData.getOriginalClient()) {
//					this.debug("Socket is original client");
//					this.debug("cancelling key...");
//					key.cancel();
//				}
//				return;
			case CLOSE:
				key.cancel();
				if(socketChannel==originalClient) {
					this.closeSession(protocol);
				} else {
					this.closeSocket(socketChannel);
				}
				return;
			}
		} catch (ClosedChannelException cce) {
			System.err.println(cce);
			if(socketChannel==originalClient) {
				this.closeSession(protocol);
				return;
			}
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
			if(socketChannel==originalClient) {
				this.closeSession(protocol);
				return;
			}
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
			this.closeSocket(socketChannel);
		} catch (CancelledKeyException cke) {
			System.err.println("Session corrupted: abort!");
			this.closeSession(protocol);
			return;
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
				this.debug("Immediate connection");
				socketData.putSocket(client, keyAttachment.getCurrentResponse().getDest());
				client.register(selector, TcpStructure.getResponseOps(currentResponse.getAction()), keyAttachment);
			} else {
				this.debug("Non Immediate connection");
				SelectionKey selectionKey = client.register(selector, SelectionKey.OP_CONNECT, keyAttachment);
				PendingConnection pendingConnection = new PendingConnection(selectionKey);
				pendingConnectionList.add(pendingConnection);
			}
		} catch(IllegalArgumentException iae) {
			System.err.println(iae);
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
			this.debug("New connection from: " + socketChannel.socket().getRemoteSocketAddress());
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
				this.closeSession(newServerProtocol);
				return;
			default:
				this.closeSession(newServerProtocol);
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
		this.debug("Reading from " + socketChannel.socket().getRemoteSocketAddress());
		try {
			int size = socketChannel.read(byteBuffer);
			if(size==-1) {
				throw new IOException("Socket closed");
			}
			this.debug(size);
			byteBuffer.flip();
			this.debug(TCSMPParser.decode(byteBuffer));
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			SocketChannel originalClient = protocolDomainMap.get(protocol).getOriginalClient();
			if(socketChannel==originalClient) {
				this.closeSession(protocol);
				return;
			} else {
				this.closeSocket(socketChannel);
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
		this.debug("Writing to " + socketChannel.socket().getRemoteSocketAddress());
		try {
			this.debug(TCSMPParser.decode(byteBuffer));
			while(byteBuffer.hasRemaining()) {
				socketChannel.write(byteBuffer);
			}
			byteBuffer.clear();
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			System.err.println(e);
			SocketChannel originalClient = protocolDomainMap.get(protocol).getOriginalClient();
			if(socketChannel==originalClient) {
				this.closeSession(protocol);
				return;
			} else {
				this.closeSocket(socketChannel);
			}
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}

	/**
	 * Selector method: connects to a socket
	 * @param key - selected key
	 * @throws ConnectException 
	 */
	private void doConnect(SelectionKey key) throws ConnectException {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		this.debug("Preparing to connect..");
		try {
			if(socketChannel.finishConnect()==false) {
				this.debug("Closing!");
				this.closeSocket(socketChannel);
				return;
			}
			this.removePendingConnection(key);
			this.debug("Connected!");
			SocketData socketData = protocolDomainMap.get(keyAttachment.getProtocol());
			socketData.putSocket(socketChannel, keyAttachment.getCurrentResponse().getDest());
			key.interestOps(TcpStructure.getResponseOps(keyAttachment.getCurrentResponse().getAction()));
		} catch (IOException e) {
			if(givenProtocol.getProtocolMode()==ProtocolMode.CLIENT) {
				throw new ConnectException();
			}
			this.debug(e);
			this.closeSocket(socketChannel);
			ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
			Protocol protocol = keyAttachment.getProtocol();
			byteBuffer.clear();
			Response cancelResponse = protocol.cancel(byteBuffer);
			this.handleResponse(key, cancelResponse);
		}
	}

	/**
	 * Checks in the pending connection list if a socketChannel is timed out
	 * If so, it closes the socketChannel
	 * @throws TimeoutException
	 */
	private void closeTimedOutPendingConnection() throws TimeoutException {
		for(PendingConnection pendingConnection: pendingConnectionList) {
			if(pendingConnection.isTimedOut()) {
				this.debug("TIME OUT!!!!");
				if(givenProtocol.getProtocolMode()==ProtocolMode.CLIENT) {
					this.closeSocket((SocketChannel)pendingConnection.getSelectionKey().channel());
					throw new TimeoutException("Connection timed out! Aborted.");
				}
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

	/**
	 * If a connection succeeds, it has to be removed from the pending connection list
	 * @param selectionKey - the selection key associated with the pending connection to remove
	 */
	private void removePendingConnection(SelectionKey selectionKey) {
		Iterator<PendingConnection> it = pendingConnectionList.iterator();
		while(it.hasNext()) {
			PendingConnection pendingConnection = it.next();
			if(pendingConnection.getSelectionKey()==selectionKey) {
				it.remove();
			}
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
//		case RELAYALL:
//			return SelectionKey.OP_WRITE;
		default:
			throw new IllegalArgumentException("No Ops for " + responseAction.name());
		}
	}

	private void closeSession(Protocol sessionProtocol) {
		this.debug("Closing session...");
		SocketData socketData = protocolDomainMap.get(sessionProtocol);
		for(SocketChannel socketChannel: socketData.getClients()) {
			this.closeSocket(socketChannel);
		}
		SocketChannel originalClient = socketData.getOriginalClient();
		this.closeSocket(originalClient);
		protocolDomainMap.remove(sessionProtocol);
		this.debug("...done!");
	}

	private void closeSocket(SocketChannel socketChannel) {
		try {
			this.debug("Closing socket...");
			socketChannel.close();
		} catch (IOException e1) {
			//nothing to do, already closed
		}
	}

	/**
	 * Use for debug purpose only
	 * @param message - the debug message
	 */
	private void debug(Object message) {
		if(debug) {
			System.out.println(" *** Tcp Structure: " + message);
		}
	}

}
