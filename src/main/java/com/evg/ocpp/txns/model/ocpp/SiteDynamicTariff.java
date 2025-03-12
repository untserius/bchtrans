package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "site_dynamicTariff")
public class SiteDynamicTariff extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long siteId;
	private long dynamicProfileId;

	public long getSiteId() {
		return siteId;
	}

	public void setSiteId(long siteId) {
		this.siteId = siteId;
	}

	public long getDynamicProfileId() {
		return dynamicProfileId;
	}

	public void setDynamicProfileId(long dynamicProfileId) {
		this.dynamicProfileId = dynamicProfileId;
	}

	@Override
	public String toString() {
		return "SiteDynamicTariff [siteId=" + siteId + ", dynamicProfileId=" + dynamicProfileId + "]";
	}

}
