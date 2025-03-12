package com.evg.ocpp.txns.model.ocpp;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
//@Indexed
@Table(name = "account_transaction")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "account" })
public class AccountTransactions extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Temporal(TemporalType.DATE)
	@Column(name = "createTimeStamp", length = 10)
	private Date createTimeStamp;
	private String transactionType;
	private double amtDebit;
	private double amtCredit;
	private double currentBalance;
	private String sessionId;
	private String status;
	private String comment;
	private String oldRefId;
	private String paymentMode;
	private Accounts account;
	private String currencyType;
	private String transactionId;
	private double currencyRate;
	private String currencySymbol;
	private Date lastUpdatedTime;
	private double tax1_amount;
	private double tax2_amount;
	private double tax3_amount;
	private double tax1_pct;
	private double tax2_pct;
	private double tax3_pct;
	
	public Date getLastUpdatedTime() {
		return lastUpdatedTime;
	}

	public void setLastUpdatedTime(Date lastUpdatedTime) {
		this.lastUpdatedTime = lastUpdatedTime;
	}

	public Date getCreateTimeStamp() {
		return createTimeStamp;
	}

	public void setCreateTimeStamp(Date createTimeStamp) {
		this.createTimeStamp = createTimeStamp;
	}

	public double getAmtDebit() {
		return amtDebit;
	}

	public void setAmtDebit(double amtDebit) {
		this.amtDebit = amtDebit;
	}

	public double getAmtCredit() {
		return amtCredit;
	}

	public void setAmtCredit(double amtCredit) {
		this.amtCredit = amtCredit;
	}

	public double getCurrentBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(double currentBalance) {
		this.currentBalance = currentBalance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.REFRESH, CascadeType.MERGE })
	@JoinColumn(name = "account_id")
	public Accounts getAccount() {
		return account;
	}

	public void setAccount(Accounts account) {
		this.account = account;
	}

	/*@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "accountTransaction")
	public Set<Session> getSessions() {
		return sessions;
	}

	public void setSessions(Set<Session> sessions) {
		this.sessions = sessions;
	}*/

	public String getOldRefId() {
		return oldRefId;
	}

	public void setOldRefId(String oldRefId) {
		this.oldRefId = oldRefId;
	}

	
	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public double getCurrencyRate() {
		return currencyRate;
	}

	public void setCurrencyRate(double currencyRate) {
		this.currencyRate = currencyRate;
	}

	public String getCurrencySymbol() {
		return currencySymbol;
	}

	public void setCurrencySymbol(String currencySymbol) {
		this.currencySymbol = currencySymbol;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}

	public double getTax1_amount() {
		return tax1_amount;
	}

	public void setTax1_amount(double tax1_amount) {
		this.tax1_amount = tax1_amount;
	}

	public double getTax2_amount() {
		return tax2_amount;
	}

	public void setTax2_amount(double tax2_amount) {
		this.tax2_amount = tax2_amount;
	}

	public double getTax3_amount() {
		return tax3_amount;
	}

	public void setTax3_amount(double tax3_amount) {
		this.tax3_amount = tax3_amount;
	}

	public double getTax1_pct() {
		return tax1_pct;
	}

	public void setTax1_pct(double tax1_pct) {
		this.tax1_pct = tax1_pct;
	}

	public double getTax2_pct() {
		return tax2_pct;
	}

	public void setTax2_pct(double tax2_pct) {
		this.tax2_pct = tax2_pct;
	}

	public double getTax3_pct() {
		return tax3_pct;
	}

	public void setTax3_pct(double tax3_pct) {
		this.tax3_pct = tax3_pct;
	}

	

	

	

	

	
}