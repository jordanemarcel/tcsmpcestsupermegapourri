package fr.umlv.tcsmp.mail.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.umlv.tcsmp.dns.DNSResolver;
import fr.umlv.tcsmp.dns.TCSMPResolver;
import fr.umlv.tcsmp.mail.Message;
import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;
import fr.umlv.tcsmp.tcp.TcpStructure;

public class GraphicTCSMPClient {

	public static JPanel createMainPanel(final JFrame parent){

		JPanel main = new JPanel(new BorderLayout());
		JPanel header = new JPanel(new GridLayout(6,2));
		JPanel center = new JPanel(new BorderLayout());
		JPanel cmd = new JPanel();
		//header
		header.add(new JLabel("Relay:"));
		final JTextField relayField = new JTextField();
		header.add(relayField);
		header.add(new JLabel("From:"));
		final JTextField fromField = new JTextField();
		header.add(fromField);
		header.add(new JLabel("To:"));
		final JTextField toField = new JTextField();
		header.add(toField);
		header.add(new JLabel("Cc:"));
		final JTextField ccField = new JTextField();
		header.add(ccField);
		header.add(new JLabel("Bcc:"));
		final JTextField bccField = new JTextField();
		header.add(bccField);
		header.add(new JLabel("Subject:"));
		final JTextField subjectField = new JTextField();
		header.add(subjectField);
		//bottom
		final JButton send = new JButton("Send");
		final JButton clear = new JButton("Clear");
		final JButton defaultConfig = new JButton("DefaultConfig");
		cmd.add(defaultConfig);
		cmd.add(clear);
		cmd.add(send);
		//Center
		final JTextArea text = new JTextArea();
		JScrollPane jsp = new JScrollPane(text);
		center.add(jsp);
		center.add(new JLabel("Body:"),BorderLayout.NORTH);
		//Total
		main.add(center,BorderLayout.CENTER);
		main.add(header,BorderLayout.NORTH);
		main.add(cmd,BorderLayout.SOUTH);
		//Action
		defaultConfig.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				relayField.setText("192.168.1.10");
				fromField.setText("jordane@client.com");
				toField.setText("jordane@jordane.com");
				ccField.setText("");
				bccField.setText("");
				subjectField.setText("Test subject");
				text.setText("");
			}
		});
		clear.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				relayField.setText("");
				fromField.setText("");
				toField.setText("");
				ccField.setText("");
				bccField.setText("");
				subjectField.setText("");
				text.setText("");
			}
		});
		send.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String mail = text.getText();
				String to = toField.getText();
				String from = fromField.getText();
				String subject = subjectField.getText();
				if(to.length()==0) {
					JOptionPane.showMessageDialog(parent, "You HAVE to fill the 'TO' field", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				if(from.length()==0) {
					JOptionPane.showMessageDialog(parent, "You HAVE to fill the 'FROM' field", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				ArrayList<String> toList = new ArrayList<String>();
				String[] dests = toField.getText().split(",");
				for(String s : dests){
					if(s.length()>1) {
						toList.add(s);
					}
				}
				ArrayList<String> ccList = new ArrayList<String>();
				dests = ccField.getText().split(",");
				for(String s : dests){
					if(s.length()>1) {
						ccList.add(s);
					}
				}
				ArrayList<String> bccList = new ArrayList<String>();
				dests = bccField.getText().split(",");
				for(String s : dests){
					if(s.length()>1) {
						bccList.add(s);
					}
				}

				DNSResolver resolver = new TCSMPResolver("127.0.0.1", "/tmp/domains");

				String header = Message.buildMailHeader(from, toList, ccList, subject);

				String clientDomain = from.substring(from.indexOf('@')+1);

				String relay = relayField.getText();
				if(relay==null) {
					try {
						relay = resolver.resolv(clientDomain).getHostAddress();
					} catch (UnknownHostException e1) {
						System.err.println("Unknown host! Setting relay to 127.0.0.1\n");
						relay = "127.0.0.1";
					}
				}
				System.out.println("Relay: " + relay);

				Protocol p = new Protocol(ProtocolMode.CLIENT, 2626);
				for(String s: toList) {
					p.addRcpt(s);
				}
				for(String s: ccList) {
					p.addRcpt(s);
				}
				for(String s: bccList) {
					p.addRcpt(s);
				}
				p.setFrom(from);
				p.setClientDomain(clientDomain);
				p.setDefaultRelay(relay);
				p.mail(header+mail+"\r\n.\r\n");

				try {
					TcpStructure tcpStructure = new TcpStructure(resolver);
					tcpStructure.processProtocol(p);
					String mainErrors = p.getMainErrors();
					Map<String, StringBuilder> domainErrors = p.getDomainErrors();
					StringBuilder errorMsg = new StringBuilder();
					errorMsg.append(mainErrors).append("\n");
					for(Map.Entry<String, StringBuilder> entry : domainErrors.entrySet()) {
						errorMsg.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
					}
					if (errorMsg.length() == 0) {
						JOptionPane.showMessageDialog(parent, errorMsg.toString(), "Error(s) sending mail", JOptionPane.ERROR_MESSAGE);
					}
				} catch (ConnectException ce) {
					JOptionPane.showMessageDialog(parent, "Connection refused!", "Error", JOptionPane.ERROR_MESSAGE);
				} catch(UnknownHostException uhe) {
					JOptionPane.showMessageDialog(parent, "Unknown host: " + relay, "Error", JOptionPane.ERROR_MESSAGE);
				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(parent, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		return main;
	}
}