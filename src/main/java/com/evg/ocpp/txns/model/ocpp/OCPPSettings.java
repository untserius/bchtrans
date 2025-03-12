package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ocpp_settings")
public class OCPPSettings extends BaseEntity{
	private static final long serialVersionUID = 1L;
	private long orgId;
	private String supportEmail;
	private String devEmails;
	private String ftpUrl;
	private String ftpDiagnosticsPath;
	private double idleBillCap;
	private long time;
	private double maximumRevenue;
	private double maxkWh;
	
	public long getOrgId() {
		return orgId;
	}
	public void setOrgId(long orgId) {
		this.orgId = orgId;
	}
	public String getSupportEmail() {
		return supportEmail;
	}
	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}
	public String getDevEmails() {
		return devEmails;
	}
	public void setDevEmails(String devEmails) {
		this.devEmails = devEmails;
	}
	public String getFtpUrl() {
		return ftpUrl;
	}
	public void setFtpUrl(String ftpUrl) {
		this.ftpUrl = ftpUrl;
	}
	public String getFtpDiagnosticsPath() {
		return ftpDiagnosticsPath;
	}
	public void setFtpDiagnosticsPath(String ftpDiagnosticsPath) {
		this.ftpDiagnosticsPath = ftpDiagnosticsPath;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public double getIdleBillCap() {
		return idleBillCap;
	}
	public void setIdleBillCap(double idleBillCap) {
		this.idleBillCap = idleBillCap;
	}
	
	public double getMaximumRevenue() {
		return maximumRevenue;
	}
	public void setMaximumRevenue(double maximumRevenue) {
		this.maximumRevenue = maximumRevenue;
	}
	
	public double getMaxkWh() {
		return maxkWh;
	}
	public void setMaxkWh(double maxkWh) {
		this.maxkWh = maxkWh;
	}
	@Override
	public String toString() {
		return "OCPPSettings [orgId=" + orgId + ", supportEmail=" + supportEmail + ", devEmails=" + devEmails
				+ ", ftpUrl=" + ftpUrl + ", ftpDiagnosticsPath=" + ftpDiagnosticsPath + ", idleBillCap=" + idleBillCap
				+ ", time=" + time + ", maximumRevenue=" + maximumRevenue + ", maxkWh=" + maxkWh + "]";
	}
}
