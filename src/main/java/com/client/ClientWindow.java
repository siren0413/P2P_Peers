
/**
 * NAME: 
 * 		ClientWindow.java
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

package com.client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JPanel;

import java.awt.GridLayout;
import java.io.File;
import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;

import com.db.PeerDB.PeerHSQLDB;
import com.rmi.api.IRegister;
import com.rmi.api.impl.PeerTransfer;
import com.util.SystemUtil;

import javax.swing.JTextField;
import javax.swing.JLabel;

import org.apache.log4j.Logger;
import javax.swing.JProgressBar;
import javax.swing.text.DefaultCaret;
import java.awt.Font;


public class ClientWindow {

	/** The logger. */
	private final Logger LOGGER = Logger.getLogger(ClientWindow.class);

	/** The frame. */
	private JFrame frame;

	/** The text area. */
	private JTextArea textArea;

	/** The instance. */
	private static ClientWindow instance;

	/** The option pane. */
	private final JOptionPane optionPane = new JOptionPane();

	/** The file chooser. */
	private final JFileChooser fileChooser = new JFileChooser();

	/** The text field_server ip. */
	private JTextField textField_serverIP;

	/** The text field_server port. */
	private JTextField textField_serverPort;

	/** The text field_download file name. */
	private JTextField textField_downloadFileName;

	/** The text field_download limit. */
	private JTextField textField_downloadLimit;

	/** The progress bar. */
	private JProgressBar progressBar;

	/** The label. */
	private JLabel label;

	// regular expression
	/** The pattern. */
	private Pattern pattern;

	/** The matcher. */
	private Matcher matcher;

	/** The ip pattern. */
	private final String IP_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	/** The port pattern. */
	private final String PORT_PATTERN = "^[\\d]{2,5}$";

	// default value
	/** The default_ ip. */
	private final String default_IP = "192.168.1.125";

	/** The default_port. */
	private final String default_port = "1099";

	// Object
	/** The peer. */
	private Peer peer;

	/** The peer registry. */
	Registry peerRegistry;

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
					ClientWindow window = ClientWindow.getInstance();
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
	private ClientWindow() {
		initialize();
		peer = new Peer(this);
		PeerHSQLDB.initDB();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		// window
		frame = new JFrame();
		frame.setResizable(false);
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnAbout = new JMenu("About...");
		menuBar.add(mnAbout);

		JMenuItem mntmAbout = new JMenuItem("About...");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(frame, "Vincent");
			}
		});
		mnAbout.add(mntmAbout);
		frame.getContentPane().setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		scrollPane.setBounds(0, 0, 794, 178);
		panel.add(scrollPane);

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setMargin(new Insets(0, 5, 0, 0));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		final JButton btnNewButton = new JButton("Share Files");
		btnNewButton.setEnabled(false);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.showOpenDialog(frame);
				File[] files = fileChooser.getSelectedFiles();
				for (File file : files) {
					if (peer.shareFile(file)) {
						textArea.append(SystemUtil.getSimpleTime() + "share file [" + file.getName() + "]\n");
					} else {
						textArea.append(SystemUtil.getSimpleTime() + "Unable to register file [" + file.getName() + "]\n");
					}
				}

			}
		});
		btnNewButton.setBounds(19, 266, 122, 26);
		panel.add(btnNewButton);

		textField_serverIP = new JTextField();
		textField_serverIP.setText(default_IP);
		textField_serverIP.setBounds(70, 189, 122, 28);
		panel.add(textField_serverIP);
		textField_serverIP.setColumns(10);

		textField_serverPort = new JTextField();
		textField_serverPort.setText(default_port);
		textField_serverPort.setBounds(236, 189, 66, 28);
		panel.add(textField_serverPort);
		textField_serverPort.setColumns(10);

		JLabel lblNewLabel = new JLabel("Server IP");
		lblNewLabel.setBounds(10, 195, 61, 16);
		panel.add(lblNewLabel);

		JLabel lblPort = new JLabel("Port");
		lblPort.setBounds(204, 195, 61, 16);
		panel.add(lblPort);

		final JButton btnDownloadFiles = new JButton("Download Files");
		btnDownloadFiles.setEnabled(false);
		btnDownloadFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final String fileName = textField_downloadFileName.getText();
				if ("".equals(fileName)) {
					JOptionPane.showMessageDialog(frame, "The file name is not valid!", "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				JOptionPane.showMessageDialog(frame, "Please select a directory to save the file", "INFO",
						JOptionPane.INFORMATION_MESSAGE);
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
					final String path = fileChooser.getSelectedFile().getAbsolutePath();
					LOGGER.info("received user saved file path:" + path);
					File file = new File(path + File.separator + fileName);
					if (file.exists()) {
						JOptionPane.showMessageDialog(frame, "The file already exits", "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}
					label.setText("Saved Path: " + path);
					Thread t = new Thread(new Runnable() {
						public void run() {
							peer.downloadFile(fileName, path + File.separator + fileName);
						}
					});
					LOGGER.info("start a new thread for downloading file.");
					t.start();

				}

			}
		});
		btnDownloadFiles.setBounds(19, 303, 122, 26);
		panel.add(btnDownloadFiles);

		textField_downloadFileName = new JTextField();
		textField_downloadFileName.setEnabled(false);
		textField_downloadFileName.setColumns(10);
		textField_downloadFileName.setBounds(151, 302, 122, 28);
		panel.add(textField_downloadFileName);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setDoubleBuffered(true);
		progressBar.setBounds(19, 340, 254, 20);
		panel.add(progressBar);
		progressBar.setVisible(false);

		textField_downloadLimit = new JTextField();
		textField_downloadLimit.setEnabled(false);
		textField_downloadLimit.setText("1");
		textField_downloadLimit.setColumns(10);
		textField_downloadLimit.setBounds(388, 302, 45, 28);
		panel.add(textField_downloadLimit);

		label = new JLabel("");
		label.setBounds(25, 383, 458, 46);
		panel.add(label);

		final JButton btnFileList = new JButton("File List");
		btnFileList.setEnabled(false);
		btnFileList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				peer.listServerFile();
			}
		});
		btnFileList.setBounds(151, 266, 122, 26);
		panel.add(btnFileList);

		JLabel lblBandwidth = new JLabel("Bandwidth");
		lblBandwidth.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblBandwidth.setBounds(317, 302, 66, 28);
		panel.add(lblBandwidth);

		JLabel lblKbs = new JLabel("KB/S");
		lblKbs.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblKbs.setBounds(443, 301, 66, 28);
		panel.add(lblKbs);

		final JButton btnClearScreen = new JButton("Clear Screen");
		btnClearScreen.setEnabled(false);
		btnClearScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		btnClearScreen.setBounds(662, 189, 122, 28);
		panel.add(btnClearScreen);

		final JButton btnConnect = new JButton("connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {

					String serverIP = textField_serverIP.getText();
					String serverPort = textField_serverPort.getText();

					pattern = Pattern.compile(IP_PATTERN);
					matcher = pattern.matcher(serverIP);
					if (!matcher.matches()) {
						JOptionPane.showMessageDialog(frame, "The IP address is not valid!", "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}
					pattern = Pattern.compile(PORT_PATTERN);
					matcher = pattern.matcher(serverPort);
					if (!matcher.matches()) {
						JOptionPane.showMessageDialog(frame, "The port is not valid!", "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}

					LOGGER.debug("invoke remote object [" + "rmi://" + serverIP + ":" + serverPort + "/register]");
					IRegister register = (IRegister) Naming.lookup("rmi://" + serverIP + ":" + serverPort + "/register");

					// register service port
					peerRegistry = LocateRegistry.createRegistry(2055);
					peerRegistry.rebind("peerTransfer", new PeerTransfer());
					LOGGER.info("open service port 2055, bind object peerTransfer");

					if (peerRegistry != null && register.registerPeer("2055")) {
						LOGGER.info("Register service port [2055] successfully!");
					} else {
						textArea.append(SystemUtil.getSimpleTime() + "Unable to register service port [2055]!\n");
						LOGGER.error("Unable to register service port [2055]!");
						return;
					}

					textArea.append(SystemUtil.getSimpleTime() + "Connected to server [" + serverIP + ":" + serverPort
							+ "] successfully!\n");
					textField_serverIP.setEnabled(false);
					textField_serverPort.setEnabled(false);
					btnConnect.setEnabled(false);

					// peer
					peer.setPeer_service_port("2055");
					peer.setServer_ip(serverIP);
					peer.setServer_port(serverPort);

					// button enable
					btnNewButton.setEnabled(true);
					btnDownloadFiles.setEnabled(true);
					btnFileList.setEnabled(true);
					btnClearScreen.setEnabled(true);
					textField_downloadFileName.setEnabled(true);
					textField_downloadLimit.setEnabled(true);

					// start thread
					Thread t = new Thread(new Runnable() {

						public void run() {
							while (true) {
								try {
									if (!peer.sendSignal()) {
										peer.sendReport();
									}
									Thread.sleep(10000);

								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						}
					});

					t.start();

					Thread t1 = new Thread(new Runnable() {

						public void run() {
							while (true) {
								try {
									peer.updateLocalDatabase();
									Thread.sleep(10000);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

						}
					});

					t1.start();

				} catch (ConnectException e1) {
					JOptionPane.showMessageDialog(frame, "unable to connect to server!\nplease make sure the address is correct",
							"ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				} catch (MalformedURLException e1) {
					JOptionPane.showMessageDialog(frame, "unable to connect to server!\nplease make sure the address is correct",
							"ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				} catch (RemoteException e1) {
					JOptionPane.showMessageDialog(frame, "unknown server error! \nplease try again later", "ERROR",
							JOptionPane.ERROR_MESSAGE);
					return;
				} catch (NotBoundException e1) {
					JOptionPane.showMessageDialog(frame, "unable to connect to server!\nplease make sure the address is correct",
							"ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
		});
		btnConnect.setBounds(317, 190, 117, 29);
		panel.add(btnConnect);

	}

	/**
	 * Gets the text area.
	 * 
	 * @return the text area
	 */
	public JTextArea getTextArea() {
		return textArea;
	}

	/**
	 * Gets the option pane.
	 * 
	 * @return the option pane
	 */
	public JOptionPane getOptionPane() {
		return optionPane;
	}

	/**
	 * Gets the file chooser.
	 * 
	 * @return the file chooser
	 */
	public JFileChooser getFileChooser() {
		return fileChooser;
	}

	/**
	 * Gets the single instance of ClientWindow.
	 * 
	 * @return single instance of ClientWindow
	 */
	public static ClientWindow getInstance() {
		if (instance == null) {
			instance = new ClientWindow();
		}
		return instance;
	}

	/**
	 * Gets the peer registry.
	 * 
	 * @return the peer registry
	 */
	public Registry getPeerRegistry() {
		return peerRegistry;
	}

	/**
	 * Gets the progress bar.
	 * 
	 * @return the progress bar
	 */
	public JProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * Gets the frame.
	 * 
	 * @return the frame
	 */
	public JFrame getFrame() {
		return frame;
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public JLabel getLabel() {
		return label;
	}

	/**
	 * Gets the text field_ download limit.
	 * 
	 * @return the text field_ download limit
	 */
	public JTextField getTextField_DownloadLimit() {
		return textField_downloadLimit;
	}
}
