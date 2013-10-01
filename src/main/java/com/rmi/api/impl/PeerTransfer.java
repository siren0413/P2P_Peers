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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.dao.PeerDAO;
import com.rmi.api.IPeerTransfer;

@SuppressWarnings("serial")
public class PeerTransfer extends UnicastRemoteObject implements IPeerTransfer {
	/*
	 * File transfer between peers
	 */

	private Logger LOGGER = Logger.getLogger(PeerTransfer.class);
	private PeerDAO peerDAO = new PeerDAO();

	public PeerTransfer() throws RemoteException {
		super();
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

}
