package fr.umlv.tcsmp.main;

import javax.swing.JFrame;

import fr.umlv.tcsmp.mail.ui.GraphicTCSMPClient;

/**
 * Simple main class to build the main frame UI that helps
 * user to send TCSMP message.
 */
public class clientTCSMP {

	public static void main(String[] args) {
		JFrame f = new JFrame("Write an Email");
		f.setSize(400,600);
		f.setContentPane(GraphicTCSMPClient.createMainPanel(f));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}

}
