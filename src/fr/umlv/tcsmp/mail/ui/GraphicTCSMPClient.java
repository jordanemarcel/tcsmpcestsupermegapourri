package fr.umlv.tcsmp.mail.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import fr.umlv.tcsmp.proto.Protocol;
import fr.umlv.tcsmp.proto.ProtocolMode;

public class GraphicTCSMPClient {
	
	public static void main(String[] args) {
		JFrame f = new JFrame("Write an Email");
		f.setSize(400,600);
		f.setContentPane(GraphicTCSMPClient.createMainPanel(f));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

	private static JPanel createMainPanel(final JFrame parent){
		JPanel main = new JPanel(new BorderLayout());
		JPanel header = new JPanel(new GridLayout(10,1));
		JPanel center = new JPanel(new BorderLayout());
		JPanel cmd = new JPanel();
		//header
		header.add(new JLabel("To:"));
		final JTextField toField = new JTextField();
		header.add(toField);
		header.add(new JLabel("Cc:"));
		final JTextField ccField = new JTextField();
		header.add(ccField);
		header.add(new JLabel("Bcc:"));
		final JTextField bccField = new JTextField();
		header.add(bccField);
		header.add(new JLabel("From:"));
		final JTextField fromField = new JTextField();
		header.add(fromField);
		header.add(new JLabel("Subject:"));
		final JTextField subjectField = new JTextField();
		header.add(subjectField);
		//bottom
		final JButton send = new JButton("Send");
		final JButton clear = new JButton("Clear");
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
		clear.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				toField.setText("");
				ccField.setText("");
				bccField.setText("");
				fromField.setText("");
				subjectField.setText("");
				text.setText("");
			}
		});
		send.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String mail = text.getText();
				String from = fromField.getText();
				String to = toField.getText();
				String subject = subjectField.getText();
				if(to.length()==0)
					JOptionPane.showMessageDialog(parent, "You HAVE to fill the 'TO' field", "Error", JOptionPane.ERROR_MESSAGE);
				if(from.length()==0)
					JOptionPane.showMessageDialog(parent, "You HAVE to fill the 'FROM' field", "Error", JOptionPane.ERROR_MESSAGE);
				
//				MailData mailData = new MailData();
//				mailData.setBody(mail);
//				mailData.setSubject(subject);
//				mailData.setFrom(from);
//				mailData.setTo(to);
//				mailData.setCc(ccField.getText());
//				System.out.println(mailData);
				
				
				ArrayList<String> ccList = new ArrayList<String>();
				String[] dests = ccField.getText().split(";");
				for(String s : dests){
					ccList.add(s);
				}
				ArrayList<String> bccList = new ArrayList<String>();
				dests = bccField.getText().split(";");
				for(String s : dests){
					bccList.add(s);
				}
				
				Protocol p = new Protocol(ProtocolMode.CLIENT);
				List<String> recpt = p.getRecpts();
				recpt.addAll(ccList);
				recpt.addAll(bccList);
				p.setFrom(from);
				
				//TODO envoi du mail
				//TODO affiche d'un message en cas de réussite ou échec
			}
		});
		return main;
	}
}