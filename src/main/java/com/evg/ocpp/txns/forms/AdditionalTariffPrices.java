package com.evg.ocpp.txns.forms;

public class AdditionalTariffPrices {
	
	private double price;
	
	private String type;
	
	private double vat;
	
	private String restrictionType;
	
	private double stepsize;

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getVat() {
		return vat;
	}

	public void setVat(double vat) {
		this.vat = vat;
	}

	public String getRestrictionType() {
		return restrictionType;
	}

	public void setRestrictionType(String restrictionType) {
		this.restrictionType = restrictionType;
	}

	public double getStepsize() {
		return stepsize;
	}

	public void setStepsize(double stepsize) {
		this.stepsize = stepsize;
	}

	@Override
	public String toString() {
		return "AdditionalTariffPrices [price=" + price + ", type=" + type + ", vat=" + vat + ", restrictionType="
				+ restrictionType + ", stepsize=" + stepsize + "]";
	}
	
}
