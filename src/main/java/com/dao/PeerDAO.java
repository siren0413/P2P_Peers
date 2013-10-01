/**
 * NAME: 
 * 		PeerDAO.java
 * 
 * PURPOSE: 
 * 		To insert, delete and find file from Peer's database. Peer database has
 * 		one table named 'PeerFiles' to store the files.
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 */
package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cache.PeerInfo;
import com.db.PeerDB.PeerHSQLDB;
import com.util.ID_Generator;

public class PeerDAO {

	/**
	 * To insert, delete and find file from Peer's database. Peer database has
	 * one table named 'PeerFiles' to store the files.
	 */
	

	/** The conn. */
	Connection conn;
	
	/** The stmt. */
	PreparedStatement stmt;
	
	/** The result. */
	ResultSet result;
	
	/** The statement. */
	Statement statement;

	
	/**
	 * insert into 'PeerFiles' table with file path,file name and file size
	 * 
	 * @param filePath
	 *            the file path
	 * @param fileName
	 *            the file name
	 * @param fileSize
	 *            the file size
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 */
	public boolean insertFile(String filePath, String fileName, int fileSize) throws SQLException {

		try {
			conn = PeerHSQLDB.getConnection();
			String id = ID_Generator.generateID();
			String sql = "insert into PeerFiles values (?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, id);
			stmt.setString(2, filePath);
			stmt.setString(3, fileName);
			stmt.setInt(4, fileSize);
			stmt.executeUpdate();

			return true;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	 
	/**
	 * delete a specific file from PeerFiles table
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 */
	public boolean deleteFile(String fileName) throws SQLException {

		try {
			conn = PeerHSQLDB.getConnection();
			statement = conn.createStatement();
			String sql = "delete from PeerFiles where file_name like '" + fileName + "'";
			statement.executeUpdate(sql);

			return true;
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	 
	/**
	 * find the file path of a specific file from 'PeerFiles'
	 * 
	 * @param fileName
	 *            the file name
	 * @return the string
	 * @throws SQLException
	 *             the sQL exception
	 */
	public String findFile(String fileName) throws SQLException {

		try {
			conn = PeerHSQLDB.getConnection();
			statement = conn.createStatement();
			String sql = "select file_path from PeerFiles where file_name like '" + fileName + "'";
			result = statement.executeQuery(sql);
			while (result.next()) {
				return result.getString(1);
			}
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	
	/**
	 *  check whether a file is in the database
	 * 
	 * @param fileName
	 *            the file name
	 * @return true, if successful
	 * @throws SQLException
	 *             the sQL exception
	 */
	public boolean checkFileAvailable(String fileName) throws SQLException {

		if (findFile(fileName) != null)
			return true;
		return false;
	}

	 
	/**
	 * get all files in the database
	 * 
	 * @return the list
	 * @throws SQLException
	 *             the sQL exception
	 */
	public List<String> selectAllFiles() throws SQLException {

		List<String> allFiles = new ArrayList<String>();
		try {
			conn = PeerHSQLDB.getConnection();
			statement = conn.createStatement();
			String sql = "select file_name from PeerFiles";
			result = statement.executeQuery(sql);
			while (result.next()) {
				allFiles.add(result.getString(1));
			}
		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return allFiles;
	}
	
	 
	/**
	 * get the peer info from database
	 * 
	 * @return the list
	 * @throws SQLException
	 *             the sQL exception
	 */
	public List<PeerInfo> queryAllfromPeerInfo () throws SQLException{
		List<PeerInfo> peerInfolist = new ArrayList<PeerInfo>();
		try {
			conn = PeerHSQLDB.getConnection();
			statement = conn.createStatement();
			String sql = "select * from PeerFiles";
			result = statement.executeQuery(sql);
			while (result.next()) {
				PeerInfo pInfo = new PeerInfo();
				pInfo.setId(result.getString(1));
				pInfo.setFilePath(result.getString(2));
				pInfo.setFileName(result.getString(3));
				pInfo.setFileSize(result.getInt(4));
				peerInfolist.add(pInfo);
			}

		} finally {
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return peerInfolist;
	}
}
