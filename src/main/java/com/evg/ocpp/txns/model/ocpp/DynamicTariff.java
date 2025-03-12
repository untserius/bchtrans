package com.evg.ocpp.txns.model.ocpp;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "dynamic_Tariff")
public class DynamicTariff extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String profileName;
	private String createdBy;
	private String modifiedBy;
	private String userTimeZone;

	private long orgid;

	@Temporal(TemporalType.DATE)
	@Column(name = "creation_date", length = 10)
	private Date createdDate = new Date();

	@Temporal(TemporalType.DATE)
	@Column(name = "creation_date", length = 10)
	private Date modifiedDate = new Date();

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public long getOrgid() {
		return orgid;
	}

	public void setOrgid(long orgid) {
		this.orgid = orgid;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getUserTimeZone() {
		return userTimeZone;
	}

	public void setUserTimeZone(String userTimeZone) {
		this.userTimeZone = userTimeZone;
	}

	@Override
	public String toString() {
		return "DynamicTariff [profileName=" + profileName + ", createdBy=" + createdBy + ", modifiedBy=" + modifiedBy
				+ ", userTimeZone=" + userTimeZone + ", orgid=" + orgid + ", createdDate=" + createdDate
				+ ", modifiedDate=" + modifiedDate + "]";
	}

}
