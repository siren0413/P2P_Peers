/**
 * NAME: 
 * 		ServerHSQLDB.java
 * 
 * PURPOSE: 
 * 		Database for index server
 *      There are two tables in there: 'PeerInfo' to save peer registry information 
 *      and 'FileInfo' for peer files management.
 * 
 * COMPUTER HARDWARE AND/OR SOFTWARE LIMITATIONS: 
 * 		JRE(1.7) required.
 * 
 * PROJECT: 
 * 		P2P File sharing system
 */

package com.db.ServerDB;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

public class ServerHSQLDB {
	/**
	 * Database for index server
	 * There are two tables in there: 'PeerInfo' to save peer registry information 
	 * and 'FileInfo' for peer files management.
	 */

	/** The logger. */
	private static Logger LOGGER = Logger.getLogger(ServerHSQLDB.class);

	/**
	 * initialize tables for server database 
	 */
	public static void initDB() {
			LOGGER.info("initializing ServerHSQLDB...");
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Connection conn = getConnection();
			String peerInfoTable = "PeerInfo";
			String fileInfoTable = "FileInfo";
			String createPeerInfo = "CREATE TABLE       "+peerInfoTable + " (" + "id         VARCHAR    NOT NULL           primary key,"
					+ "ip         VARCHAR      NOT NULL			," + "port       VARCHAR         NOT NULL, "
					+ " constraint peer_id_unique UNIQUE ( ip )"+ ")";
			
			String createFileInfo = "CREATE TABLE       " + fileInfoTable + "  (" 
					+" peer_id  		VARCHAR 	NOT NULL, " +  "	file_name 	VARCHAR 	NOT NULL,"    
					+" FOREIGN KEY (peer_id) REFERENCES " + peerInfoTable + " ( id ) ," 
					+" constraint both_unique UNIQUE ( peer_id,file_name ) )" ;
			
			if(!checkTableExists(conn, peerInfoTable)) {
				LOGGER.info("Table "+ peerInfoTable + " dose not exits.");
				Statement stat = conn.createStatement();
				stat.executeUpdate(createPeerInfo);
				stat.close();
				LOGGER.info("Table " + peerInfoTable + " creates successfully.");
				
			}
			
			if(true) {
				LOGGER.info("Table "+ fileInfoTable + " dose not exits.");
				Statement stat = conn.createStatement();
				stat.executeUpdate(createFileInfo);
				stat.close();
				LOGGER.info("Table " + fileInfoTable + " creates successfully.");
				
			}
			
			conn.close();
			LOGGER.info("initializing ServerHSQLDB Successfully!");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
	}
	
	 
	/**
	 * get connection to the database
	 * 
	 * @return the connection
	 * @throws SQLException
	 *             the sQL exception
	 */
	public static Connection getConnection() throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:serverhsqldb/db", "SA", "");
		return conn;
	}
	
	/**
	 * check whether a table is exits
	 * 
	 * @param conn
	 *            the connection
	 * @param tableName
	 *            the table name
	 * @return true, if successful
	 */
	public static boolean checkTableExists (Connection conn, String tableName) {
		boolean checkTable = false;
		
		try {
			DatabaseMetaData metaData = conn.getMetaData();
			ResultSet resultSet = metaData.getTables(null, null, tableName,null);
			while(resultSet.next()) {
				checkTable = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return checkTable;
	}

}
