package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dynamicTariff_Month")
public class DynamicTariffMonth extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long dynamicProfileId;

	private long monthId;

	public long getDynamicProfileId() {
		return dynamicProfileId;
	}

	public void setDynamicProfileId(long dynamicProfileId) {
		this.dynamicProfileId = dynamicProfileId;
	}

	public long getMonthId() {
		return monthId;
	}

	public void setMonthId(long monthId) {
		this.monthId = monthId;
	}

	@Override
	public String toString() {
		return "DynamicTariffMonth [dynamicProfileId=" + dynamicProfileId + ", monthId=" + monthId + "]";
	}

}
