/**
 * NAME: 
 * 		Peer.java
 * 
 * PURPOSE: 
 * 		implement all the Peer operations.
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 * 
 * ALGORITHM DESCRIPTION: 
 * 		implement peer operations:
 * 		1. downloadFile -- download file from other peer.
 * 		2. shareFile -- share file with other peers.
 * 		3. sendSignal -- send signal to server make sure the data is consistent.
 * 		4. sendReport -- send report to server when data is not consistent.
 * 		5. listServerFile -- list all the files that available to download.
 * 		6. updateLocalDatabase -- update local database when file delete from disk.
 * 
 */
package com.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.cache.PeerInfo;
import com.dao.PeerDAO;
import com.rmi.api.IHeartBeat;
import com.rmi.api.IPeerTransfer;
import com.rmi.api.IRegister;
import com.rmi.api.IServerTransfer;
import com.util.ID_Generator;
import com.util.PropertyUtil;
import com.util.SystemUtil;

/**
 * The Class Peer.
 */
public class Peer {

	/** The logger. */
	private final Logger LOGGER = Logger.getLogger(Peer.class);

	/** The window. */
	private PeerWindow window;

	/** The server ip. */
	private String serverIP;

	/** The server port. */
	private String serverPort;

	/** The peer_service_port. */
	private String peer_service_port;

	/** The peer dao. */
	private PeerDAO peerDAO;

	/**
	 * Instantiates a new peer.
	 * 
	 * @param window
	 *            the window
	 */
	public Peer(PeerWindow window) {
		this.window = window;
		peerDAO = new PeerDAO();
	}

	/**
	 * Share file.
	 * 
	 * @param file
	 *            the file
	 * @return true, if successful
	 */
	public boolean shareFile(File file) {
		try {
			// add the file to self database
			boolean result2 = peerDAO.insertFile(file.getAbsolutePath(), file.getName(), 100);
			if (result2)
				LOGGER.info("insert file[" + file.getName() + "] to local database successfully!");
			else
				return false;

		} catch (SQLException e) {
			LOGGER.error("Unable to register file [" + file.getName() + "] due to DAO error", e);
			return false;
		}

		return true;
	}

	private class QueryProcess implements Runnable {
		private String fileName;

		public QueryProcess(String fileName) {
			super();
			this.fileName = fileName;
		}
		public void run() {
			try {
				Date time_insert = new Date(System.currentTimeMillis());
				Date time_expire = new Date(time_insert.getTime() + 10 * 1000);
				String messageId = ID_Generator.generateID();
				peerDAO.addMessage(messageId, InetAddress.getLocalHost().getHostAddress(), peer_service_port, time_insert, time_expire, fileName);

				LOGGER.info("Add message to database. ip:" + InetAddress.getLocalHost().getHostAddress() + " port:" + peer_service_port + " file:" + fileName);

				PropertyUtil propertyUtil = new PropertyUtil("network.properties");
				Collection<Object> values = propertyUtil.getProperties();
				for (Object obj : values) {
					IPeerTransfer peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + obj + "/peerTransfer");
					peerTransfer.query(messageId, 10, fileName, peer_service_port);
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/**
	 * Download file.
	 * 
	 * @param fileName
	 *            the file name
	 * @param savePath
	 *            the save path
	 * @return true, if successful
	 */
	public boolean downloadFile(String fileName, String savePath) {

		Thread queryProcess = new Thread(new QueryProcess(fileName));
		queryProcess.start();

		boolean result = false;
		String messageId = null;

		while (true) {

			String element = null;
			String[] destAddr = null;
			try {
				element = window.getDownloadingQueue().take();
				destAddr = element.split(":");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}
			String ip = destAddr[0];
			String port = destAddr[1];
			messageId = destAddr[2];
			String file_name = destAddr[3];
			
			if(!file_name.equals(fileName)) {
				LOGGER.debug("Destory previous downloading thread due to fileName not equal. expect["+fileName+"], was["+file_name+"]");
				try {
					window.getDownloadingQueue().put(element);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return false;
			}
			LOGGER.debug("invoke remote object [" + "rmi://" + ip + ":" + port + "/peerTransfer]");
			IPeerTransfer peerTransfer;
			try {
				peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + ip + ":" + port + "/peerTransfer");
			} catch (Exception e1) {
				e1.printStackTrace();
				continue;
			}

			int length = 0;
			try {
				length = peerTransfer.getFileLength(fileName);
			} catch (RemoteException e1) {
				e1.printStackTrace();
				continue;
			}
			int start = 0;
			int left = length;
			LOGGER.info("file size:" + length + " bytes");

			File file = new File(savePath);
			OutputStream out;
			try {
				out = new FileOutputStream(file);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
				continue;
			}

			byte[] buffer;
			window.getProgressBar().setMaximum(length);
			window.getProgressBar().setVisible(true);
			window.getProgressBar().setStringPainted(true);

			LOGGER.info("download speed:" + Integer.valueOf(window.getTextField_DownloadLimit().getText()) + " KB/S");

			window.getTextArea().append(SystemUtil.getSimpleTime() + "Start downloading...\n");

			while (left > 0) {
				try {
					Thread.sleep(1000);

					buffer = peerTransfer.obtain(fileName, start, 1024 * Integer.valueOf(window.getTextField_DownloadLimit().getText()));

					out.write(buffer);
					left -= buffer.length;
					start += buffer.length;
					window.getProgressBar().setValue(start);
					window.getProgressBar().setIndeterminate(false);
					window.getProgressBar().repaint();
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}
			
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			result = true;

			if (result) {
				LOGGER.info("download file successfully!");
				window.getTextArea().append(SystemUtil.getSimpleTime() + "Download complete!\n");
				try {
					peerDAO.removeMessage(messageId);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				window.getDownloadingQueue().clear();
			} else {
				LOGGER.info("fail to download.");
				window.getTextArea().append(SystemUtil.getSimpleTime() + "Download abort!\n");
			}
			window.getProgressBar().setVisible(false);

		}

	}

	/**
	 * Send signal.
	 * 
	 * @return true, if successful
	 */
	public boolean sendSignal() {
		try {
			LOGGER.info("sending heartbeat signal to index server");
			LOGGER.debug("invoke remote object [" + "rmi://" + serverIP + ":" + serverPort + "/heartBeat]");
			IHeartBeat heartBeat = (IHeartBeat) Naming.lookup("rmi://" + serverIP + ":" + serverPort + "/heartBeat");
			List<String> listFiles = peerDAO.selectAllFiles();
			Collections.sort(listFiles);
			MessageDigest md = MessageDigest.getInstance("MD5");
			if (!heartBeat.signal(md.digest(listFiles.toString().getBytes()), peer_service_port)) {
				LOGGER.info("peer data is not consistent with server data, request sync.");
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		LOGGER.info("peer data is consistent with server data.");
		return true;
	}

	/**
	 * Send report.
	 * 
	 * @return true, if successful
	 */
	public boolean sendReport() {
		try {
			LOGGER.info("sending heartbeat report to sync with index server");
			LOGGER.debug("invoke remote object [" + "rmi://" + serverIP + ":" + serverPort + "/heartBeat]");
			IHeartBeat heartBeat = (IHeartBeat) Naming.lookup("rmi://" + serverIP + ":" + serverPort + "/heartBeat");
			List<String> listFiles = peerDAO.selectAllFiles();
			heartBeat.report(listFiles);
			LOGGER.info("sent report successfully.");
		} catch (Exception e) {
			LOGGER.error("fail to send report", e);
		}

		return false;
	}

	/**
	 * List server file.
	 */
	public void listServerFile() {
		try {
			LOGGER.debug("invoke remote object [" + "rmi://" + serverIP + ":" + serverPort + "/serverTransfer]");
			IServerTransfer serverTransfer = (IServerTransfer) Naming.lookup("rmi://" + serverIP + ":" + serverPort + "/serverTransfer");
			List<String> files = serverTransfer.listAllFile();
			LOGGER.debug("got file list from index server.");
			window.getTextArea().append(SystemUtil.getSimpleTime() + "****************** Available File List *******************\n");
			for (String file : files) {
				window.getTextArea().append(SystemUtil.getSimpleTime() + file + "\n");
			}
			window.getTextArea().append(SystemUtil.getSimpleTime() + "**********************************************************\n");

		} catch (Exception e) {
			LOGGER.error("fail to receive file list from server", e);
		}

	}

	/**
	 * Update local database.
	 */

	public void updateLocalDatabase() {
		LOGGER.info("update local database");
		try {
			List<PeerInfo> list = peerDAO.queryAllfromPeerInfo();
			for (PeerInfo info : list) {
				File file = new File(info.getFilePath());
				if (!file.exists()) {
					LOGGER.info("file not found [" + file.getAbsolutePath() + "]");
					peerDAO.deleteFile(info.getFileName());
					LOGGER.info("delete file [" + file.getName() + "] from database.");
				}
			}

		} catch (SQLException e) {
			LOGGER.error("DAO error", e);
		}
	}

	/**
	 * Gets the server_ip.
	 * 
	 * @return the server_ip
	 */
	public String getServer_ip() {
		return serverIP;
	}

	/**
	 * Sets the server_ip.
	 * 
	 * @param server_ip
	 *            the new server_ip
	 */
	public void setServer_ip(String server_ip) {
		this.serverIP = server_ip;
	}

	/**
	 * Gets the server_port.
	 * 
	 * @return the server_port
	 */
	public String getServer_port() {
		return serverPort;
	}

	/**
	 * Sets the server_port.
	 * 
	 * @param server_port
	 *            the new server_port
	 */
	public void setServer_port(String server_port) {
		this.serverPort = server_port;
	}

	/**
	 * Gets the peer_service_port.
	 * 
	 * @return the peer_service_port
	 */
	public String getPeer_service_port() {
		return peer_service_port;
	}

	/**
	 * Sets the peer_service_port.
	 * 
	 * @param peer_service_port
	 *            the new peer_service_port
	 */
	public void setPeer_service_port(String peer_service_port) {
		this.peer_service_port = peer_service_port;
	}

}
