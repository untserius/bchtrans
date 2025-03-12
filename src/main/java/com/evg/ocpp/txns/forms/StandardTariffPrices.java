package com.evg.ocpp.txns.forms;

public class StandardTariffPrices {
	
    private double price;
	
	private double step;
	
	private String type;
	
	private double tax_excl;
	
	private double tax_incl;

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getTax_excl() {
		return tax_excl;
	}

	public void setTax_excl(double tax_excl) {
		this.tax_excl = tax_excl;
	}

	public double getTax_incl() {
		return tax_incl;
	}

	public void setTax_incl(double tax_incl) {
		this.tax_incl = tax_incl;
	}

	@Override
	public String toString() {
		return "StandardTariffPrices [price=" + price + ", step=" + step + ", type=" + type + ", tax_excl=" + tax_excl
				+ ", tax_incl=" + tax_incl + "]";
	}
}
