package com.evg.ocpp.txns.model.ocpp;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "session_energy")
public class SessionEnergy extends BaseEntity{

	private static final long serialVersionUID = 1L;
	
	private String sessionId;
	
	private double power;
	
	private double kwh;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public double getPower() {
		return power;
	}

	public void setPower(double power) {
		this.power = power;
	}

	public double getKwh() {
		return kwh;
	}

	public void setKwh(double kwh) {
		this.kwh = kwh;
	}

	@Override
	public String toString() {
		return "SessionEnergy [sessionId=" + sessionId + ", power=" + power + ", kwh=" + kwh + "]";
	}
	
}
