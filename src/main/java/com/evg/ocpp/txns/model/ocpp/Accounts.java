package com.evg.ocpp.txns.model.ocpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true, value = "user")
public class Accounts extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String accountName;

	private String oldRefId;

	@Temporal(TemporalType.DATE)
	@Column(name = "creation_date", length = 10)
	private Date creationDate;

	@Temporal(TemporalType.DATE)
	@Column(name = "modified_date", length = 10)
	private Date modifiedDate;

	@Column(name = "activeAccount", nullable = false)
	private boolean activeAccount;

	private boolean autoReload;
	private User user;

	@Column(nullable = false)
	private double accountBalance;
	
	private boolean lowBalanceFlag;
	
	@Column(columnDefinition = "tinyint(1) default 0")
	private boolean notificationFlag;

	private List<AccountTransactions> accountTransactions = new ArrayList<AccountTransactions>(0);

	private List<Credentials> credentials = new ArrayList<Credentials>(0);

	private List<Vehicles> vehicles = new ArrayList<Vehicles>(0);

	private String currencyType;
	
	private String currencySymbol;
	
	public Accounts(){
		
	}
	public Accounts(String accountName) {
		this.accountName = accountName;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public boolean isActiveAccount() {
		return activeAccount;
	}

	public void setActiveAccount(boolean activeAccount) {
		this.activeAccount = activeAccount;
	}

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
	@JoinColumn(name = "user_id")
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public double getAccountBalance() {
		return accountBalance;
	}

	public void setAccountBalance(double accountBalance) {
		this.accountBalance = accountBalance;
	}

	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "account")
	@Fetch(value = FetchMode.SUBSELECT)
	public List<AccountTransactions> getAccountTransactions() {
		return accountTransactions;
	}

	public void setAccountTransactions(List<AccountTransactions> accountTransactions) {
		this.accountTransactions = accountTransactions;
	}

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "account")
	@Fetch(value = FetchMode.SUBSELECT)
	public List<Credentials> getCredentials() {
		return credentials;
	}

	public void setCredentials(List<Credentials> credentials) {
		this.credentials = credentials;
	}

	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "account")
	public List<Vehicles> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<Vehicles> vehicles) {
		this.vehicles = vehicles;
	}

	public String getOldRefId() {
		return oldRefId;
	}

	public void setOldRefId(String oldRefId) {
		this.oldRefId = oldRefId;
	}
	
	
	@Column(name = "autoReload", columnDefinition = "bit default 1 not null")
	public boolean isAutoReload() {
		return autoReload;
	}

	public void setAutoReload(boolean autoReload) {
		this.autoReload = autoReload;
	}

	public boolean isLowBalanceFlag() {
		return lowBalanceFlag;
	}

	public void setLowBalanceFlag(boolean lowBalanceFlag) {
		this.lowBalanceFlag = lowBalanceFlag;
	}
	
	public boolean getNotificationFlag() {
		return notificationFlag;
	}
	public void setNotificationFlag(boolean notificationFlag) {
		this.notificationFlag = notificationFlag;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	
	public String getCurrencySymbol() {
		return currencySymbol;
	}
	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public Date getModifiedDate() {return modifiedDate;}
	public void setModifiedDate(Date modifiedDate) {this.modifiedDate = modifiedDate;}

	@Override
	public String toString() {
		return "Accounts [accountName=" + accountName + ", oldRefId=" + oldRefId + ", creationDate=" + creationDate
				+ ", activeAccount=" + activeAccount + ", autoReload=" + autoReload + ", user=" + user
				+ ", accountBalance=" + accountBalance + ", lowBalanceFlag=" + lowBalanceFlag + ", notificationFlag="
				+ notificationFlag + ", accountTransactions=" + accountTransactions + ", credentials=" + credentials
				+ ", vehicles=" + vehicles + ", currencyType=" + currencyType + ", currencySymbol=" + currencySymbol
				+ "]";
	}
	
	
}
