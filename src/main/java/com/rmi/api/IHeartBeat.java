/*
 * 
 */
package com.rmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote method to test whether a peer is alive by send out a 
 * signal.   
 */
public interface IHeartBeat extends Remote {


	
	/**
	 * send signal to peer.
	 * 
	 * @param MD5_array
	 *            the md5_array
	 * @param peer_service_port
	 *            the peer_service_port
	 * @return true, if successful
	 * @throws RemoteException
	 *             the remote exception
	 */
	public boolean signal(byte[] MD5_array, String peer_service_port) throws RemoteException;
	
	/**
	 * if the peer's files are not the same as registered on index server report it.
	 * 
	 * @param fileList
	 *            the file list
	 * @throws RemoteException
	 *             the remote exception
	 */
	public void report(List<String> fileList) throws RemoteException;
	
}
