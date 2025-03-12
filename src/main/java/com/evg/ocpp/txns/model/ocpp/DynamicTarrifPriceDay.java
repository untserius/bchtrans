package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "dynamicTarrifPrice_Day")
public class DynamicTarrifPriceDay extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long dynamicPriceId;
	private long dayId;

	public long getDynamicPriceId() {
		return dynamicPriceId;
	}

	public void setDynamicPriceId(long dynamicPriceId) {
		this.dynamicPriceId = dynamicPriceId;
	}

	public long getDayId() {
		return dayId;
	}

	public void setDayId(long dayId) {
		this.dayId = dayId;
	}

	@Override
	public String toString() {
		return "DynamicTarrifPriceDay [dynamicPriceId=" + dynamicPriceId + ", dayId=" + dayId + "]";
	}

}
