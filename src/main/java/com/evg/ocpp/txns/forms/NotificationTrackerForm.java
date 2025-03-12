package com.evg.ocpp.txns.forms;

public class NotificationTrackerForm {
	private String sessionId;
	private String type;
	private String notifyType;
	private long userId;
 
	private long account_transactionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(String notifyType) {
		this.notifyType = notifyType;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getAccount_transactionId() {
		return account_transactionId;
	}

	public void setAccount_transactionId(long account_transactionId) {
		this.account_transactionId = account_transactionId;
	}

	@Override
	public String toString() {
		return "NotificationTrackerForm [sessionId=" + sessionId + ", type=" + type + ", notifyType=" + notifyType
				+ ", userId=" + userId + ", account_transactionId=" + account_transactionId + "]";
	}
	
	
}
