package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;


@Entity
@Table(name = "ocpi_price")
public class Price {

	/**
	 * 
	 */
	private String id;

	@Id
	@GenericGenerator(name = "generator", strategy = "uuid2", parameters = {})
	@GeneratedValue(generator = "generator")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	private double excl_vat;

	private double incl_vat;

	public double getExcl_vat() {
		return excl_vat;
	}

	public void setExcl_vat(double excl_vat) {
		this.excl_vat = excl_vat;
	}

	public double getIncl_vat() {
		return incl_vat;
	}

	public void setIncl_vat(double incl_vat) {
		this.incl_vat = incl_vat;
	}

	@Override
	public String toString() {
		return "Price [excl_vat=" + excl_vat + ", incl_vat=" + incl_vat + "]";
	}
}
