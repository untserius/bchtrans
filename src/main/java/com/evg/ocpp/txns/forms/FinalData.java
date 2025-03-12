package com.evg.ocpp.txns.forms;

public class FinalData {
	
	private Long firstValue;
	private String secondValue;
	private String thirdValue;
	private StopTransaction stopTransaction;
	private MeterValues meterValues;
	private String stnReferNum;

	
	public Long getFirstValue() {
		return firstValue;
	}

	public void setFirstValue(Long firstValue) {
		this.firstValue = firstValue;
	}

	public String getSecondValue() {
		return secondValue;
	}

	public void setSecondValue(String secondValue) {
		this.secondValue = secondValue;
	}

	public String getThirdValue() {
		return thirdValue;
	}

	public void setThirdValue(String thirdValue) {
		this.thirdValue = thirdValue;
	}

	public MeterValues getMeterValues() {
		return meterValues;
	}

	public void setMeterValues(MeterValues meterValues) {
		this.meterValues = meterValues;
	}

	public StopTransaction getStopTransaction() {
		return stopTransaction;
	}

	public void setStopTransaction(StopTransaction stopTransaction) {
		this.stopTransaction = stopTransaction;
	}

	public String getStnReferNum() {
		return stnReferNum;
	}

	public void setStnReferNum(String stnReferNum) {
		this.stnReferNum = stnReferNum;
	}

	@Override
	public String toString() {
		return "FinalData [firstValue=" + firstValue + ", secondValue=" + secondValue + ", thirdValue=" + thirdValue
				+ ", stopTransaction=" + stopTransaction + ", meterValues=" + meterValues + ", stnReferNum="
				+ stnReferNum + "]";
	}
	
	
}
