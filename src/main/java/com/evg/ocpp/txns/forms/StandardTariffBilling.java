package com.evg.ocpp.txns.forms;

public class StandardTariffBilling {

    private long sessionUniqueId;
	
	private long elementId;
	
	private long priceId;
	
	private String priceType;
	
	private double price;
	
	private double vatPer;
	
	private double vatValue;
	
	private double stepSize;

	private double cost;
	
	private double costWithVat;
	
	private double value;

	
	
	public long getPriceId() {
		return priceId;
	}

	public void setPriceId(long priceId) {
		this.priceId = priceId;
	}

	public long getSessionUniqueId() {
		return sessionUniqueId;
	}

	public void setSessionUniqueId(long sessionUniqueId) {
		this.sessionUniqueId = sessionUniqueId;
	}

	public long getElementId() {
		return elementId;
	}

	public void setElementId(long elementId) {
		this.elementId = elementId;
	}

	public String getPriceType() {
		return priceType;
	}

	public void setPriceType(String priceType) {
		this.priceType = priceType;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getVatPer() {
		return vatPer;
	}

	public void setVatPer(double vatPer) {
		this.vatPer = vatPer;
	}

	public double getVatValue() {
		return vatValue;
	}

	public void setVatValue(double vatValue) {
		this.vatValue = vatValue;
	}

	public double getStepSize() {
		return stepSize;
	}

	public void setStepSize(double stepSize) {
		this.stepSize = stepSize;
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

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "StandardTariffBilling [sessionUniqueId=" + sessionUniqueId + ", elementId=" + elementId + ", priceId="
				+ priceId + ", priceType=" + priceType + ", price=" + price + ", vatPer=" + vatPer + ", vatValue="
				+ vatValue + ", stepSize=" + stepSize + ", cost=" + cost + ", costWithVat=" + costWithVat + ", value="
				+ value + "]";
	}

}
