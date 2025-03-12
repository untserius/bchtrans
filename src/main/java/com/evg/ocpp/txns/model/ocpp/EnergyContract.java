package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ocpi_energy_contract")
public class EnergyContract extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String supplier_name;

	private String contract_id;

	public String getSupplier_name() {
		return supplier_name;
	}

	public void setSupplier_name(String supplier_name) {
		this.supplier_name = supplier_name;
	}

	public String getContract_id() {
		return contract_id;
	}

	public void setContract_id(String contract_id) {
		this.contract_id = contract_id;
	}

	@Override
	public String toString() {
		return "EnergyContract [supplier_name=" + supplier_name + ", contract_id=" + contract_id + "]";
	}

}
