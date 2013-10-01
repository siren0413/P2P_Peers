/**
 * NAME: 
 * 		ServerWindow.java
 * 
 * PURPOSE: 
 * 		Open a GUI window for interacting with users
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 * 
 * ALGORITHM DESCRIPTION: 
 * 		initialize java swing frame and all the components in the frame.
 * 		implement the event handler of some action such as button clicked.
 * 
 */
package com.server;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JButton;

import com.db.ServerDB.ServerHSQLDB;
import com.rmi.api.impl.HeartBeat;
import com.rmi.api.impl.Register;
import com.rmi.api.impl.ServerTransfer;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

import javax.swing.JTextArea;

import java.awt.SystemColor;

// TODO: Auto-generated Javadoc
/**
 * ServerWindow to start the server.
 */
public class ServerWindow {

	/** The frame. */
	private JFrame frame;

	/**
	 * Launch the application.
	 * 
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerWindow window = new ServerWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the application.
	 */
	public ServerWindow() {
		initialize();
		ServerHSQLDB.initDB();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.getContentPane().setBackground(SystemColor.control);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		final JTextArea textArea = new JTextArea();
		textArea.setBackground(SystemColor.control);
		textArea.setEditable(false);
		textArea.setBounds(89, 180, 254, 44);
		frame.getContentPane().add(textArea);
		
		final JButton btnNewButton = new JButton("Start Index Server");
		btnNewButton.setEnabled(true);
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnNewButton.setEnabled(false);
				
				// RMI registry
				try {
					
					LocateRegistry.createRegistry(1099);
					Naming.rebind("register",new Register());
					Naming.rebind("heartBeat",new HeartBeat());
					Naming.rebind("serverTransfer", new ServerTransfer());
					
					textArea.append("Index server is running...");

				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				
				
			}
		});
		btnNewButton.setBounds(79, 55, 269, 113);
		frame.getContentPane().add(btnNewButton);
		
		
	}
}
