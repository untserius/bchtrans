package com.evg.ocpp.txns.forms;

public class TransactionForm {
	
	private String message="";
	
	private String clientId="0";

	private String messageType="";
	
	private String sessionId;
	
	private long stnId;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public long getStnId() {
		return stnId;
	}

	public void setStnId(long stnId) {
		this.stnId = stnId;
	}

	@Override
	public String toString() {
		return "TransactionForm [message=" + message + ", clientId=" + clientId + ", messageType=" + messageType
				+ ", sessionId=" + sessionId + ", stnId=" + stnId + "]";
	}

	

	
	
	
}
