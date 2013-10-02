/**
 * NAME: 
 * 		PeerTransfer.java
 * 
 * PURPOSE: 
 * 		RMI class. operations between peer and peer.
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 * 
 * ALGORITHM DESCRIPTION: 
 * 		1. obtain -- get byte array of the file from other peer.
 * 		2. getFileLength -- get the length of file which to be download
 * 		3. getFilePath -- get the file path.
 * 		4. checkFileAvailable -- check if the file is exist on the peer.
 * 
 */

package com.rmi.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import com.cache.PeerMessage;
import com.client.PeerWindow;
import com.dao.PeerDAO;
import com.rmi.api.IPeerTransfer;
import com.util.PropertyUtil;

@SuppressWarnings("serial")
public class PeerTransfer extends UnicastRemoteObject implements IPeerTransfer {
	/*
	 * File transfer between peers
	 */

	private Logger LOGGER = Logger.getLogger(PeerTransfer.class);
	private PeerDAO peerDAO = new PeerDAO();
	private PeerWindow window;

	public PeerTransfer(PeerWindow window) throws RemoteException {
		super();
		this.window = window;
	}

	// download a file from a peer
	public byte[] obtain(String fileName, int start, int length) throws RemoteException {

		// get byte[] from other peers;
		try {
			String filePath = peerDAO.findFile(fileName);
			InputStream is = new FileInputStream(filePath);
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			byte[] buffer = new byte[length];
			int readSize;
			is.skip(start);
			if ((readSize = is.read(buffer, 0, length)) != -1) {
				byteArray.write(buffer, 0, readSize);
			}
			is.close();
			return byteArray.toByteArray();

		} catch (FileNotFoundException e) {
			LOGGER.error("file: " + fileName + " not found", e);
			return null;
		} catch (IOException e) {
			LOGGER.error("unable to read file", e);
			return null;
		} catch (SQLException e) {
			LOGGER.error("DAO error", e);
		}
		return null;

	}

	public int getFileLength(String fileName) throws RemoteException {
		String filePath;
		File file = null;
		try {
			filePath = peerDAO.findFile(fileName);
			file = new File(filePath);
		} catch (SQLException e) {
			LOGGER.error("DAO error", e);
		}
		return (int) file.length();
	}

	public String getFilePath(String fileName) {
		try {
			return peerDAO.findFile(fileName);
		} catch (SQLException e) {
			LOGGER.error("DAO error", e);
		}
		return null;
	}

	public boolean checkFileAvailable(String fileName) throws RemoteException {
		try {
			return peerDAO.checkFileAvailable(fileName);
		} catch (SQLException e) {
			LOGGER.error("DAO error", e);
		}
		return false;
	}

	public void query(String messageId, int TTL, String fileName, String service_port) throws RemoteException {
		String clienthost;
		try {
			clienthost = RemoteServer.getClientHost();

			InetAddress ia = java.net.InetAddress.getByName(clienthost);
			String clentIp = ia.getHostAddress();

			LOGGER.info("Received peer message. peer IP[" + clentIp + "]");

			if (TTL == 0) {
				LOGGER.debug("TTL=" + TTL + " query expire, looping back to sender.");
				LOGGER.debug("invoke remote object [" + "rmi://" + clentIp + ":" + service_port + "/peerTransfer]");
				IPeerTransfer peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + clentIp + ":" + service_port + "/peerTransfer");
				peerTransfer.queryExpire(messageId);
			} else {
				TTL -= 1;
				if (peerDAO.checkMessage(messageId)) {
					LOGGER.debug("messageid:" + messageId + " already exist.");
					return;
				} else {
					Date time_insert = new Date(System.currentTimeMillis());
					Date time_expire = new Date(time_insert.getTime() + TTL * 1000);
					peerDAO.addMessage(messageId, clentIp, service_port, time_insert, time_expire, fileName);
					LOGGER.debug("add message to database, message from peer:" + clentIp);
				}

				// check if the ip and port and message is already in local
				// database. if yes, then ignore, if no, continue;
				// put ip and port and messageId into local database
				// query local database see if we have the file.

				if (peerDAO.checkFileAvailable(fileName)) {
					LOGGER.debug("hitquery, looping back to sender.");
					LOGGER.debug("invoke remote object [" + "rmi://" + clentIp + ":" + service_port + "/peerTransfer]");
					IPeerTransfer peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + clentIp + ":" + service_port + "/peerTransfer");
					peerTransfer.hitQuery(messageId, TTL, fileName, InetAddress.getLocalHost().getHostAddress(), "2055");

				}

				// forward to neighbors
				PropertyUtil propertyUtil = new PropertyUtil("network.properties");
				Collection<Object> values = propertyUtil.getProperties();
				for (Object obj : values) {
					IPeerTransfer peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + obj + "/peerTransfer");
					peerTransfer.query(messageId, TTL, fileName, "2055");
				}

			}

		} catch (ServerNotActiveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void hitQuery(String messageId, int TTL, String fileName, String peerIP, String peerPort) throws RemoteException {
		try {
			PeerMessage msg = peerDAO.getPeerMessage(messageId);
			if (msg == null) {
				LOGGER.debug("message:" + messageId + " already deleted.");
				return;
			}
			if (msg.getUpstream_ip().equals(InetAddress.getLocalHost().getHostAddress())) {
				// the original sender, and put the peerIP and peerPort and
				// fileName in queue.
				window.getDownloadingQueue().put(peerIP+":"+peerPort+":"+messageId+":"+fileName);
				
			} else {
				String upstream_ip = msg.getUpstream_ip();
				String upstream_port = msg.getUpstream_port();
				LOGGER.debug("invoke remote object [" + "rmi://" + upstream_ip + ":" + upstream_port + "/peerTransfer]");
				IPeerTransfer peerTransfer = (IPeerTransfer) Naming.lookup("rmi://" + upstream_ip + ":" + upstream_port + "/peerTransfer");
				peerTransfer.hitQuery(messageId, TTL, fileName, peerIP, peerPort);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void queryExpire(String messageId) throws RemoteException {

	}

}
