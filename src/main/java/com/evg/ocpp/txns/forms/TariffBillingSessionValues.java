package com.evg.ocpp.txns.forms;

import java.util.List;

import com.evg.ocpp.txns.model.ocpp.Price;

public class TariffBillingSessionValues {
	
	private List<StandardTariffBilling>  standardTariffBilling;
	private List<AdvancedTarifBilling> advancedTarifBilling;
	private NewTariffBilling newTariffBilling;
	private double total_fixed_cost;
	private double total_energy_cost;
	private double total_time_cost;
	private double finalCosttostore=0.0;
	private double ocpi_final_vat=0.0;
	private Price price;

	public List<StandardTariffBilling> getStandardTariffBilling() {
		return standardTariffBilling;
	}

	public void setStandardTariffBilling(List<StandardTariffBilling> standardTariffBilling) {
		this.standardTariffBilling = standardTariffBilling;
	}

	public List<AdvancedTarifBilling> getAdvancedTarifBilling() {
		return advancedTarifBilling;
	}

	public void setAdvancedTarifBilling(List<AdvancedTarifBilling> advancedTarifBilling) {
		this.advancedTarifBilling = advancedTarifBilling;
	}

	public NewTariffBilling getNewTariffBilling() {
		return newTariffBilling;
	}

	public void setNewTariffBilling(NewTariffBilling newTariffBilling) {
		this.newTariffBilling = newTariffBilling;
	}

	public double getTotal_fixed_cost() {
		return total_fixed_cost;
	}

	public void setTotal_fixed_cost(double total_fixed_cost) {
		this.total_fixed_cost = total_fixed_cost;
	}

	public double getTotal_energy_cost() {
		return total_energy_cost;
	}

	public void setTotal_energy_cost(double total_energy_cost) {
		this.total_energy_cost = total_energy_cost;
	}

	public double getTotal_time_cost() {
		return total_time_cost;
	}

	public void setTotal_time_cost(double total_time_cost) {
		this.total_time_cost = total_time_cost;
	}

	public Price getPrice() {
		return price;
	}

	public void setPrice(Price price) {
		this.price = price;
	}
	

	public double getFinalCosttostore() {
		return finalCosttostore;
	}

	public void setFinalCosttostore(double finalCosttostore) {
		this.finalCosttostore = finalCosttostore;
	}

	public double getOcpi_final_vat() {
		return ocpi_final_vat;
	}

	public void setOcpi_final_vat(double ocpi_final_vat) {
		this.ocpi_final_vat = ocpi_final_vat;
	}

	@Override
	public String toString() {
		return "TariffBillingSessionValues [standardTariffBilling=" + standardTariffBilling + ", advancedTarifBilling="
				+ advancedTarifBilling + ", newTariffBilling=" + newTariffBilling + ", total_fixed_cost="
				+ total_fixed_cost + ", total_energy_cost=" + total_energy_cost + ", total_time_cost=" + total_time_cost
				+ ", finalCosttostore=" + finalCosttostore + ", ocpi_final_vat=" + ocpi_final_vat + ", price=" + price
				+ "]";
	}
}
