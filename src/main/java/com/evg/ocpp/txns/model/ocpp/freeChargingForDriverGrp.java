package com.evg.ocpp.txns.model.ocpp;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="freeChargingForDriverGrp")
public class freeChargingForDriverGrp extends BaseEntity{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Date createdDate;
	private long userId;
	private double usedFreeMins;
	private double usedFreekWhs;
	private double remainingFreeKwh;
	private double remainingFreeMins;

	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public long getUserId() {
		return userId;
	}
	public void setUserId(long userId) {
		this.userId = userId;
	}
	public double getUsedFreeMins() {
		return usedFreeMins;
	}
	public void setUsedFreeMins(double usedFreeMins) {
		this.usedFreeMins = usedFreeMins;
	}
	public double getUsedFreekWhs() {
		return usedFreekWhs;
	}
	public void setUsedFreekWhs(double usedFreekWhs) {
		this.usedFreekWhs = usedFreekWhs;
	}

	public double getRemainingFreeKwh() {
		return remainingFreeKwh;
	}

	public void setRemainingFreeKwh(double remainingFreeKwh) {
		this.remainingFreeKwh = remainingFreeKwh;
	}

	public double getRemainingFreeMins() {
		return remainingFreeMins;
	}

	public void setRemainingFreeMins(double remainingFreeMins) {
		this.remainingFreeMins = remainingFreeMins;
	}

	@Override
	public String toString() {
		return "freeChargingForDriverGrp [createdDate=" + createdDate + ", userId=" + userId + ", usedFreeMins="
				+ usedFreeMins + ", usedFreekWhs=" + usedFreekWhs + "]";
	}
}
