/*
 * 
 */
package com.rmi.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * To search available peer of  a given file.
 * 
 */
public interface IServerTransfer  extends Remote{

	/**
	 * search a file in the index server.
	 * 
	 * @param fileName
	 *            the file name
	 * @return List<String>
	 * @throws RemoteException
	 *             the remote exception
	 */
	public List<String> searchFile(String fileName) throws RemoteException;
	
	
	/**
	 * list all files in the index server.
	 * 
	 * @return List<String>
	 * @throws RemoteException
	 *             the remote exception
	 */
	public List<String> listAllFile() throws RemoteException;
	
}
