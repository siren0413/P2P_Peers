/*
 * 
 */
package com.rmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote method to register a peer, unregister a peer and register a peer's file
 * 
 */
public interface IRegister extends Remote {
	

	/**
	 * Register a peer.
	 * 
	 * @param regPort
	 *            the reg port
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean registerPeer(String regPort) throws RemoteException;
	
	/**
	 * Unregister a peer.
	 * 
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean unRegisterPeer() throws RemoteException;
	
	/**
	 * Register a file.
	 * 
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean registerFile(String fileName) throws RemoteException;
	 
}
