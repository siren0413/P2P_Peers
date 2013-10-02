package com.cache;

import java.util.Date;

public class PeerMessage {

	private String message_id;
	private String upstream_ip;
	private String upstream_port;
	private Date time_insert;
	private Date time_expire;
	private String fileName;
	
	
	public PeerMessage() {
		super();
	}
	
	
	
	
	public PeerMessage(String message_id, String upstream_ip, String upstream_port, Date time_insert, Date time_expire, String fileName) {
		super();
		this.message_id = message_id;
		this.upstream_ip = upstream_ip;
		this.upstream_port = upstream_port;
		this.time_insert = time_insert;
		this.time_expire = time_expire;
		this.fileName = fileName;
	}




	public String getMessage_id() {
		return message_id;
	}
	public void setMessage_id(String message_id) {
		this.message_id = message_id;
	}
	public String getUpstream_ip() {
		return upstream_ip;
	}
	public void setUpstream_ip(String upstream_ip) {
		this.upstream_ip = upstream_ip;
	}
	public String getUpstream_port() {
		return upstream_port;
	}
	public void setUpstream_port(String upstream_port) {
		this.upstream_port = upstream_port;
	}
	public Date getTime_insert() {
		return time_insert;
	}
	public void setTime_insert(Date time_insert) {
		this.time_insert = time_insert;
	}
	public Date getTime_expire() {
		return time_expire;
	}
	public void setTime_expire(Date time_expire) {
		this.time_expire = time_expire;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	
	
	
}
