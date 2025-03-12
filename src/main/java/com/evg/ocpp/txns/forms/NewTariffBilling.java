package com.evg.ocpp.txns.forms;

import java.util.Date;

public class NewTariffBilling {

    private long tariffId;
	
	private String tariffName;
	
	private double cost;
	
	private double costWithVat;
	
	private double finalCost;
	
	private double finalCostWithVat;
	
	private double parkingPrice;
	
	private double parkingFee;
	
	private double parkingFeeWithVat;
	
	private double parkingVatPer;
	
	private double parkingVatValue;
	
	private double parkingStepSize;
	
	private long sessionUniqueId;
	
	private boolean standardPrice;
	
	private boolean advancedPrice;
	
	private Date startDate;
	
	private Date endDate;
	
	private double kwhUsed;
	
	private double duration;
	
	private double minValue;
	private double maxValue;
	private double lastKwhValue;

	public long getTariffId() {
		return tariffId;
	}

	public void setTariffId(long tariffId) {
		this.tariffId = tariffId;
	}

	public String getTariffName() {
		return tariffName;
	}

	public void setTariffName(String tariffName) {
		this.tariffName = tariffName;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public double getCostWithVat() {
		return costWithVat;
	}

	public void setCostWithVat(double costWithVat) {
		this.costWithVat = costWithVat;
	}

	public double getFinalCost() {
		return finalCost;
	}

	public void setFinalCost(double finalCost) {
		this.finalCost = finalCost;
	}

	public double getFinalCostWithVat() {
		return finalCostWithVat;
	}

	public void setFinalCostWithVat(double finalCostWithVat) {
		this.finalCostWithVat = finalCostWithVat;
	}

	public double getParkingPrice() {
		return parkingPrice;
	}

	public void setParkingPrice(double parkingPrice) {
		this.parkingPrice = parkingPrice;
	}

	public double getParkingFee() {
		return parkingFee;
	}

	public void setParkingFee(double parkingFee) {
		this.parkingFee = parkingFee;
	}

	public double getParkingFeeWithVat() {
		return parkingFeeWithVat;
	}

	public void setParkingFeeWithVat(double parkingFeeWithVat) {
		this.parkingFeeWithVat = parkingFeeWithVat;
	}

	public double getParkingVatPer() {
		return parkingVatPer;
	}

	public void setParkingVatPer(double parkingVatPer) {
		this.parkingVatPer = parkingVatPer;
	}

	public double getParkingVatValue() {
		return parkingVatValue;
	}

	public void setParkingVatValue(double parkingVatValue) {
		this.parkingVatValue = parkingVatValue;
	}

	public double getParkingStepSize() {
		return parkingStepSize;
	}

	public void setParkingStepSize(double parkingStepSize) {
		this.parkingStepSize = parkingStepSize;
	}

	public long getSessionUniqueId() {
		return sessionUniqueId;
	}

	public void setSessionUniqueId(long sessionUniqueId) {
		this.sessionUniqueId = sessionUniqueId;
	}

	public boolean isStandardPrice() {
		return standardPrice;
	}

	public void setStandardPrice(boolean standardPrice) {
		this.standardPrice = standardPrice;
	}

	public boolean isAdvancedPrice() {
		return advancedPrice;
	}

	public void setAdvancedPrice(boolean advancedPrice) {
		this.advancedPrice = advancedPrice;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public double getKwhUsed() {
		return kwhUsed;
	}

	public void setKwhUsed(double kwhUsed) {
		this.kwhUsed = kwhUsed;
	}

	public double getDuration() {
		return duration;
	}

	public void setDuration(double duration) {
		this.duration = duration;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getLastKwhValue() {
		return lastKwhValue;
	}

	public void setLastKwhValue(double lastKwhValue) {
		this.lastKwhValue = lastKwhValue;
	}

	@Override
	public String toString() {
		return "NewTariffBilling [tariffId=" + tariffId + ", tariffName=" + tariffName + ", cost=" + cost
				+ ", costWithVat=" + costWithVat + ", finalCost=" + finalCost + ", finalCostWithVat=" + finalCostWithVat
				+ ", parkingPrice=" + parkingPrice + ", parkingFee=" + parkingFee + ", parkingFeeWithVat="
				+ parkingFeeWithVat + ", parkingVatPer=" + parkingVatPer + ", parkingVatValue=" + parkingVatValue
				+ ", parkingStepSize=" + parkingStepSize + ", sessionUniqueId=" + sessionUniqueId + ", standardPrice="
				+ standardPrice + ", advancedPrice=" + advancedPrice + ", startDate=" + startDate + ", endDate="
				+ endDate + ", kwhUsed=" + kwhUsed + ", duration=" + duration + ", minValue=" + minValue + ", maxValue="
				+ maxValue + ", lastKwhValue=" + lastKwhValue + "]";
	}
	
}
