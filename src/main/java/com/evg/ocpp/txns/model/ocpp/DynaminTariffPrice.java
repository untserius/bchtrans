package com.evg.ocpp.txns.model.ocpp;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "dynaminTariffPrice")
public class DynaminTariffPrice extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private Time startTime;
	private Time endTime;
	private double price;
	private String priceType;
	private String chargerType;

	@Transient
	private long[] day;

	@Transient
	private String startTimes;

	@Transient
	private String endTimes;

	@JoinColumn(name = "startTime")
	@ManyToOne(fetch = FetchType.EAGER)
	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {

		this.startTime = startTime;
	}

	@JoinColumn(name = "endTime")
	@ManyToOne(fetch = FetchType.EAGER)
	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {

		this.endTime = endTime;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getPriceType() {
		return priceType;
	}

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	@Transient
	public long[] getDay() {
		return day;
	}

	public void setDay(long[] day) {
		this.day = day;
	}

	@Transient
	public String getStartTimes() {
		return startTimes;
	}

	public void setStartTimes(String startTimes) {
		this.startTimes = startTimes;
	}

	@Transient
	public String getEndTimes() {
		return endTimes;
	}

	public void setEndTimes(String endTimes) {
		this.endTimes = endTimes;
	}

	@Column(name = "chargerType")
	public String getChargerType() {
		return chargerType;
	}

	public void setChargerType(String chargerType) {
		this.chargerType = chargerType;
	}

	
	@Override
	public String toString() {
		return "DynaminTariffPrice [startTime=" + startTime + ", endTime=" + endTime + ", price=" + price
				+ ", priceType=" + priceType + ", chargerType=" + chargerType + ", day=" + Arrays.toString(day)
				+ ", startTimes=" + startTimes + ", endTimes=" + endTimes + "]";
	}

}
