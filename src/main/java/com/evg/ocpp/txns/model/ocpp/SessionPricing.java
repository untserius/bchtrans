package com.evg.ocpp.txns.model.ocpp;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "session_Pricing")
public class SessionPricing extends BaseEntity{

	private static final long serialVersionUID = 1L;
	
	private long sessionUniqueId;
	
	private long dynamicProfileId;
	
	private long dynamicPriceId;
	
	private double totalEnergyCost;
		
	private double portPrice;
	
	private String portPriceUnit;
	
	private double sessionDurationInMin;
	
	private double totalKwUsed;
	
	private Date startTimeStamp;
	
	private Date endTimeStamp;
	
	private String tariffStartTime;
	
	private String tariffEndTime;
	
	private String randomSessionId;
	
	private double cost;
	
	private String dynamicProfileName;

	public long getSessionUniqueId() {
		return sessionUniqueId;
	}

	public void setSessionUniqueId(long sessionUniqueId) {
		this.sessionUniqueId = sessionUniqueId;
	}

	public long getDynamicProfileId() {
		return dynamicProfileId;
	}

	public void setDynamicProfileId(long dynamicProfileId) {
		this.dynamicProfileId = dynamicProfileId;
	}

	public long getDynamicPriceId() {
		return dynamicPriceId;
	}

	public void setDynamicPriceId(long dynamicPriceId) {
		this.dynamicPriceId = dynamicPriceId;
	}

	public double getTotalEnergyCost() {
		return totalEnergyCost;
	}

	public void setTotalEnergyCost(double totalEnergyCost) {
		this.totalEnergyCost = totalEnergyCost;
	}


	public double getPortPrice() {
		return portPrice;
	}

	public void setPortPrice(double portPrice) {
		this.portPrice = portPrice;
	}

	public String getPortPriceUnit() {
		return portPriceUnit;
	}

	public void setPortPriceUnit(String portPriceUnit) {
		this.portPriceUnit = portPriceUnit;
	}

	public double getSessionDurationInMin() {
		return sessionDurationInMin;
	}

	public void setSessionDurationInMin(double sessionDurationInMin) {
		this.sessionDurationInMin = sessionDurationInMin;
	}

	public double getTotalKwUsed() {
		return totalKwUsed;
	}

	public void setTotalKwUsed(double totalKwUsed) {
		this.totalKwUsed = totalKwUsed;
	}

	public String getRandomSessionId() {
		return randomSessionId;
	}

	public void setRandomSessionId(String randomSessionId) {
		this.randomSessionId = randomSessionId;
	}

	public Date getStartTimeStamp() {
		return startTimeStamp;
	}

	public void setStartTimeStamp(Date startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public Date getEndTimeStamp() {
		return endTimeStamp;
	}

	public void setEndTimeStamp(Date endTimeStamp) {
		this.endTimeStamp = endTimeStamp;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public String getDynamicProfileName() {
		return dynamicProfileName;
	}

	public void setDynamicProfileName(String dynamicProfileName) {
		this.dynamicProfileName = dynamicProfileName;
	}

	public String getTariffStartTime() {
		return tariffStartTime;
	}

	public void setTariffStartTime(String tariffStartTime) {
		this.tariffStartTime = tariffStartTime;
	}

	public String getTariffEndTime() {
		return tariffEndTime;
	}

	public void setTariffEndTime(String tariffEndTime) {
		this.tariffEndTime = tariffEndTime;
	}

	@Override
	public String toString() {
		return "SessionPricing [sessionUniqueId=" + sessionUniqueId + ", dynamicProfileId=" + dynamicProfileId
				+ ", dynamicPriceId=" + dynamicPriceId + ", totalEnergyCost=" + totalEnergyCost + ", portPrice="
				+ portPrice + ", portPriceUnit=" + portPriceUnit + ", sessionDurationInMin=" + sessionDurationInMin
				+ ", totalKwUsed=" + totalKwUsed + ", startTimeStamp=" + startTimeStamp + ", endTimeStamp="
				+ endTimeStamp + ", tariffStartTime=" + tariffStartTime + ", tariffEndTime=" + tariffEndTime
				+ ", randomSessionId=" + randomSessionId + ", cost=" + cost + ", dynamicProfileName="
				+ dynamicProfileName + "]";
	}
}
