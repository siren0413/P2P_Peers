/**
 * NAME: 
 * 		Register.java
 * 
 * PURPOSE: 
 * 		RMI class. register peer to server.
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 * 
 * ALGORITHM DESCRIPTION: 
 * 		1. registerPeer -- register peer info to server database and establish connection.
 * 		2. unRegisterPeer -- unregister peer and remove the info from database.
 * 		3. getFilePath -- get the file path.
 * 		4. checkFileAvailable -- check if the file is exist on the peer.
 * 
 */
package com.rmi.api.impl;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import org.apache.log4j.Logger;
import com.dao.ServerDAO;
import com.rmi.api.IRegister;

@SuppressWarnings("serial")
public class Register extends UnicastRemoteObject implements IRegister {
	/*
	 * Implementation of remote method to register a peer, unregister a peer 
	 * and register a peer's file
	 */

	public Register() throws RemoteException {
		super();
	}

	private Logger LOGGER = Logger.getLogger(Register.class);
	private ServerDAO serverDAO = new ServerDAO();
	
	
	public boolean registerPeer(String regPort) {
		String clienthost;
		try {
			// get client IP address
			clienthost = RemoteServer.getClientHost();
			InetAddress ia = java.net.InetAddress.getByName(clienthost);
			String clentIp = ia.getHostAddress();
			LOGGER.info("Received peer registry request. client IP[" + clentIp + "]");
			
			String peer_id = serverDAO.getPeerID(clentIp);
			if(peer_id==null) {
				boolean result = serverDAO.addPeer(clentIp, regPort);
				if(!result)
					LOGGER.warn("Client registry failed!");
				else {
					LOGGER.info("Registered peer ip["+clentIp+"] service port["+regPort+"] successfully!");
					return true;
				}
			}
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn("Client registry failed!");
		}
		return false;
	}

	public boolean unRegisterPeer() {
		String clienthost;
		try {
			// get client IP address
			clienthost = RemoteServer.getClientHost();
			InetAddress ia = java.net.InetAddress.getByName(clienthost);
			String clentIp = ia.getHostAddress();
			LOGGER.info("Received peer unregistry request. client IP[" + clentIp + "]");
			
			boolean result = serverDAO.deletePeer(clienthost);
			if(!result)
				LOGGER.warn("Client unregistry failed!");
			else {
				LOGGER.info("Removed peer: ip["+clentIp+"] successfully from the index server!");
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.warn("Client unregistry failed!");
		}
		return false;

	}

	public boolean registerFile(String fileName) throws RemoteException {
		String clienthost;
		try {
			// get client IP address
			clienthost = RemoteServer.getClientHost();
			InetAddress ia = java.net.InetAddress.getByName(clienthost);
			String clentIp = ia.getHostAddress();
			LOGGER.info("Received client add file request. client IP[" + clentIp + "]");
			
			boolean result = serverDAO.addFile(clentIp, fileName);
			if(!result)
				LOGGER.warn("Client add file failed !");
			else {
				LOGGER.info("Added file["+fileName+"] successfully!");
				return true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			LOGGER.warn("Client add file failed !");
		}
		return false;
	}

}
