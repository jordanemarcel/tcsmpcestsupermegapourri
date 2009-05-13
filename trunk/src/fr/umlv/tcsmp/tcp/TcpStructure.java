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

import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.Response;
import fr.umlv.tcsmp.proto.ResponseAction;

public class TcpStructure {
	public static int BUFFER_SIZE = 1024;

	private final Selector selector;
	private final TCSMPResolver dnsResolver;
	private Protocol serverProtocol;
	private final HashMap<Protocol, SocketData> protocolDomainMap = 
		new HashMap<Protocol, SocketData>();

	public TcpStructure() throws IOException {
		this(new TCSMPResolver());
	}

	public TcpStructure(TCSMPResolver dnsResolver) throws IOException {
		this.selector = Selector.open();
		this.dnsResolver = dnsResolver;
	}

	public void processProtocol(Protocol protocol) throws IOException {
		switch (protocol.getProtocolMode()) {
		case CLIENT:

			break;
		case SERVER:
			this.serverProtocol = protocol;
			this.startServer(protocol.getProtocolPort());
			break;
		}
	}

	public void startServer(int port) throws IOException {
		ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
		InetSocketAddress localIsa = new InetSocketAddress(port);
		serverSocketChannel.socket().bind(localIsa);
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		this.handleSelector();
	}

	public void handleSelector() throws IOException {
		int nbKeysSelected;
		while(true) {
			do {
				nbKeysSelected = selector.select();
			} while(nbKeysSelected<1);

			Set<SelectionKey> selectionKeys = selector.selectedKeys();
			for(SelectionKey key: selectionKeys) {
				if(key.isValid() && key.isAcceptable()) {
					this.doAccept(key);
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

	private void handleResponse(SelectionKey key, Response response) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		Protocol protocol = keyAttachment.getProtocol();
		SocketData socketData = protocolDomainMap.get(protocol);
		
		try {
			switch (response.getAction()) {
			case READ:
				// TODO domain == null
				key.interestOps(SelectionKey.OP_READ);
				break;
			case REPLY:
				SocketChannel originalClient = socketData.getOriginalClient();
				if(socketChannel==originalClient) {
					key.interestOps(SelectionKey.OP_WRITE);
				} else {
					key.cancel();
					originalClient.register(selector, SelectionKey.OP_WRITE, keyAttachment);
				}
				break;
			case RELAY:
				String domain = response.getDest();
				SocketChannel domainClient = socketData.getSocket(domain);
				if(domainClient!=null) {
					if(socketChannel==domainClient) {
						key.interestOps(SelectionKey.OP_WRITE);
					} else {
						key.cancel();
						domainClient.register(selector, SelectionKey.OP_WRITE, keyAttachment);
					}
				} else {
					key.cancel();
					keyAttachment.setCurrentResponse(response);
					this.connectNewClient(domain, keyAttachment);
				}
				break;
			case RELAYALL:
				Collection<SocketChannel> allClient = protocolDomainMap.get(protocol).getClients();
				for(SocketChannel client: allClient) {
					if(client==socketChannel) {
						key.interestOps(SelectionKey.OP_WRITE);
					} else {
						ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
						client.register(selector, SelectionKey.OP_WRITE, new KeyAttachment(byteBuffer, protocol));
					}
				}
				if(socketChannel==socketData.getOriginalClient()) {
					key.cancel();
				}
				break;
			case CLOSE:
				socketChannel.close();
				return;
			}
		} catch (ClosedChannelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void connectNewClient(String domain, KeyAttachment keyAttachment) {
		Protocol protocol = keyAttachment.getProtocol();
		try {
			InetAddress domainAddress = dnsResolver.resolv(domain);
			InetSocketAddress remoteIsa = new InetSocketAddress(domainAddress, protocol.getProtocolPort());
			SocketChannel client = SocketChannel.open();
			Response currentResponse = keyAttachment.getCurrentResponse();
			client.configureBlocking(false);
			if(client.connect(remoteIsa)) {
				client.register(selector, this.getResponseOps(currentResponse.getAction()), keyAttachment);
			} else {
				client.register(selector, SelectionKey.OP_CONNECT, keyAttachment);
			}
		} catch(IllegalArgumentException iae) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getResponseOps(ResponseAction responseAction) {
		switch (responseAction) {
		case READ:
			return SelectionKey.OP_READ;
		case REPLY:
			return SelectionKey.OP_WRITE;
		case RELAY:
			return SelectionKey.OP_WRITE;
		case RELAYALL:
			return SelectionKey.OP_WRITE;
		default:
			throw new IllegalArgumentException("No Ops for " + responseAction.name());
		}
	}

	public void doAccept(SelectionKey key) {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			System.out.println("New connection from: " + socketChannel.socket().getRemoteSocketAddress());
			Protocol newServerProtocol = serverProtocol.newProtocol();
			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
			KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, newServerProtocol);
			SocketData socketData = new SocketData(socketChannel);
			protocolDomainMap.put(newServerProtocol, socketData);
			socketChannel.configureBlocking(false);
			Response response = newServerProtocol.doIt(byteBuffer);
			switch (response.getAction()) {
			case READ:
				socketChannel.register(selector, SelectionKey.OP_READ, keyAttachment);
				break;
			case REPLY:
				socketChannel.register(selector, SelectionKey.OP_WRITE, keyAttachment);
				break;
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

	public void doRead(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel)key.channel();
		KeyAttachment keyAttachment = (KeyAttachment)key.attachment();
		ByteBuffer byteBuffer = keyAttachment.getByteBuffer();
		Protocol protocol = keyAttachment.getProtocol();
		try {
			socketChannel.read(byteBuffer);
			Response response = protocol.doIt(byteBuffer);
			this.handleResponse(key, response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void doWrite(SelectionKey key) {

	}
	
	public void doConnect(SelectionKey key) {

	}

}
