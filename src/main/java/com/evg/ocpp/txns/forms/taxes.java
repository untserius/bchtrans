package com.evg.ocpp.txns.forms;

public class taxes {

	private String tax_name_per;
	private String taxAmount;
	public String getTax_name_per() {
		return tax_name_per;
	}
	public void setTax_name_per(String tax_name_per) {
		this.tax_name_per = tax_name_per;
	}
	public String getTaxAmount() {
		return taxAmount;
	}
	public void setTaxAmount(String taxAmount) {
		this.taxAmount = taxAmount;
	}
	@Override
	public String toString() {
		return "taxes [tax_name_per=" + tax_name_per + ", taxAmount=" + taxAmount + "]";
	}
	
	
}
