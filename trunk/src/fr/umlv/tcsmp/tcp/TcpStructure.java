package fr.umlv.tcsmp.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Set;

import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.proto.Protocol;

public class TcpStructure {
	public static int BUFFER_SIZE = 1024;
	
	private final Selector selector;
	private final TCSMPResolver dnsResolver;
	private Protocol serverProtocol;
	private final HashMap<Protocol, HashMap<String, SocketChannel>> protocolDomainMap = 
		new HashMap<Protocol, HashMap<String,SocketChannel>>();
	
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
		do {
			nbKeysSelected = selector.select();
		} while(nbKeysSelected<1);
		
		Set<SelectionKey> selectionKeys = selector.keys();
		for(SelectionKey key: selectionKeys) {
			if(key.isAcceptable()) {
				this.doAccept(key);
			}
		}
		
	}
	
	public void doAccept(SelectionKey key) {
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel)key.channel();
		try {
			SocketChannel socketChannel = serverSocketChannel.accept();
			System.out.println("New connection from: " + socketChannel.socket().getRemoteSocketAddress());
			//Protocol newServerProtocol = serverProtocol.getCopy();

			ByteBuffer byteBuffer = ByteBuffer.allocateDirect(TcpStructure.BUFFER_SIZE);
			KeyAttachment keyAttachment = new KeyAttachment(byteBuffer, newServerProtocol);
			HashMap<String, SocketChannel> domainSocketMap = new HashMap<String, SocketChannel>();
			domainSocketMap.put("...", socketChannel);
			protocolDomainMap.put(newServerProtocol, domainSocketMap);
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_READ, keyAttachment);
		} catch (IOException e) {
			System.err.println("Could not accept a new connection");
			System.err.println(e);
			return;
		}
	}
	
}
