package com.evg.ocpp.txns.model.ocpp;


import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "preferredNotification")
public class PreferredNotification extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private long userId;
	
	private boolean smsLowWalletBalance;

	private boolean smsChargingCompleted;

	private boolean smsChargingInterrupted;	 
	
	private boolean smsIdleBilling;

	private boolean emailLowWalletBalance;

	private boolean emailChargingCompleted;

	private boolean emailChargingInterrupted;
	
	private boolean emailIdleBilling;

	private boolean notificationLowWalletBalance;

	private boolean notificationChargingCompleted;

	private boolean notificationChargingInterrupted;
	
	private boolean notificationIdleBilling;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public boolean isSmsLowWalletBalance() {
		return smsLowWalletBalance;
	}

	public void setSmsLowWalletBalance(boolean smsLowWalletBalance) {
		this.smsLowWalletBalance = smsLowWalletBalance;
	}

	public boolean isSmsChargingCompleted() {
		return smsChargingCompleted;
	}

	public void setSmsChargingCompleted(boolean smsChargingCompleted) {
		this.smsChargingCompleted = smsChargingCompleted;
	}

	public boolean isSmsChargingInterrupted() {
		return smsChargingInterrupted;
	}

	public void setSmsChargingInterrupted(boolean smsChargingInterrupted) {
		this.smsChargingInterrupted = smsChargingInterrupted;
	}

	public boolean isEmailLowWalletBalance() {
		return emailLowWalletBalance;
	}

	public void setEmailLowWalletBalance(boolean emailLowWalletBalance) {
		this.emailLowWalletBalance = emailLowWalletBalance;
	}

	public boolean isEmailChargingCompleted() {
		return emailChargingCompleted;
	}

	public void setEmailChargingCompleted(boolean emailChargingCompleted) {
		this.emailChargingCompleted = emailChargingCompleted;
	}

	public boolean isEmailChargingInterrupted() {
		return emailChargingInterrupted;
	}

	public void setEmailChargingInterrupted(boolean emailChargingInterrupted) {
		this.emailChargingInterrupted = emailChargingInterrupted;
	}

	public boolean isNotificationLowWalletBalance() {
		return notificationLowWalletBalance;
	}

	public void setNotificationLowWalletBalance(boolean notificationLowWalletBalance) {
		this.notificationLowWalletBalance = notificationLowWalletBalance;
	}

	public boolean isNotificationChargingCompleted() {
		return notificationChargingCompleted;
	}

	public void setNotificationChargingCompleted(boolean notificationChargingCompleted) {
		this.notificationChargingCompleted = notificationChargingCompleted;
	}

	public boolean isNotificationChargingInterrupted() {
		return notificationChargingInterrupted;
	}

	public void setNotificationChargingInterrupted(boolean notificationChargingInterrupted) {
		this.notificationChargingInterrupted = notificationChargingInterrupted;
	}

	public boolean isSmsIdleBilling() {
		return smsIdleBilling;
	}

	public void setSmsIdleBilling(boolean smsIdleBilling) {
		this.smsIdleBilling = smsIdleBilling;
	}

	public boolean isEmailIdleBilling() {
		return emailIdleBilling;
	}

	public void setEmailIdleBilling(boolean emailIdleBilling) {
		this.emailIdleBilling = emailIdleBilling;
	}

	public boolean isNotificationIdleBilling() {
		return notificationIdleBilling;
	}

	public void setNotificationIdleBilling(boolean notificationIdleBilling) {
		this.notificationIdleBilling = notificationIdleBilling;
	}

	@Override
	public String toString() {
		return "PreferredNotification [userId=" + userId + ", smsLowWalletBalance=" + smsLowWalletBalance
				+ ", smsChargingCompleted=" + smsChargingCompleted + ", smsChargingInterrupted="
				+ smsChargingInterrupted + ", smsIdleBilling=" + smsIdleBilling + ", emailLowWalletBalance="
				+ emailLowWalletBalance + ", emailChargingCompleted=" + emailChargingCompleted
				+ ", emailChargingInterrupted=" + emailChargingInterrupted + ", emailIdleBilling=" + emailIdleBilling
				+ ", notificationLowWalletBalance=" + notificationLowWalletBalance + ", notificationChargingCompleted="
				+ notificationChargingCompleted + ", notificationChargingInterrupted=" + notificationChargingInterrupted
				+ ", notificationIdleBilling=" + notificationIdleBilling + "]";
	}
	
}
