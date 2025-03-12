package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dynamicTarrif_dynamicPrice")
public class DynamicTarrifDynamicPrice extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long dynamicProfileId;
	private long dynamicPriceId;

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

	@Override
	public String toString() {
		return "DynamicTarrifDynamicPrice [dynamicProfileId=" + dynamicProfileId + ", dynamicPriceId=" + dynamicPriceId
				+ "]";
	}

}
