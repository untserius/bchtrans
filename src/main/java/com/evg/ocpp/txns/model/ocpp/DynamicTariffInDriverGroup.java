package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dynamicTariff_in_drivergroup")
public class DynamicTariffInDriverGroup extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long driverGroupId;
	private long dynamicProfileId;

	public long getDriverGroupId() {
		return driverGroupId;
	}

	public void setDriverGroupId(long driverGroupId) {
		this.driverGroupId = driverGroupId;
	}

	public long getDynamicProfileId() {
		return dynamicProfileId;
	}

	public void setDynamicProfileId(long dynamicProfileId) {
		this.dynamicProfileId = dynamicProfileId;
	}

	@Override
	public String toString() {
		return "DynamicTariffInDriverGroup [driverGroupId=" + driverGroupId + ", dynamicProfileId=" + dynamicProfileId
				+ "]";
	}

}
