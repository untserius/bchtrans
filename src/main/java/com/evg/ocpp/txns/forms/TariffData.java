package com.evg.ocpp.txns.forms;

public class TariffData {
	
	private long tariffId;
	
	private long maxPriceId;
	
	private long minPriceId;
	
	private String tariffName;
	
	private String startTime;
	
	private String endTime;
	
	private StandardTariffPrices energyPrice;
	
	private StandardTariffPrices timePrice;
	
	private StandardTariffPrices flatPrice;
	
	private StandardTariffPrices parkingPrice;
	
    private AdditionalTariffPrices rateRider;
	
	private AdditionalTariffPrices idleCharge;
	
	private AdditionalTariffPrices TAX;
	
	public long getTariffId() {
		return tariffId;
	}

	public void setTariffId(long tariffId) {
		this.tariffId = tariffId;
	}

	public long getMaxPriceId() {
		return maxPriceId;
	}

	public void setMaxPriceId(long maxPriceId) {
		this.maxPriceId = maxPriceId;
	}

	public long getMinPriceId() {
		return minPriceId;
	}

	public void setMinPriceId(long minPriceId) {
		this.minPriceId = minPriceId;
	}

	public String getTariffName() {
		return tariffName;
	}

	public void setTariffName(String tariffName) {
		this.tariffName = tariffName;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public StandardTariffPrices getEnergyPrice() {
		return energyPrice;
	}

	public void setEnergyPrice(StandardTariffPrices energyPrice) {
		this.energyPrice = energyPrice;
	}

	public StandardTariffPrices getTimePrice() {
		return timePrice;
	}

	public void setTimePrice(StandardTariffPrices timePrice) {
		this.timePrice = timePrice;
	}

	public StandardTariffPrices getFlatPrice() {
		return flatPrice;
	}

	public void setFlatPrice(StandardTariffPrices flatPrice) {
		this.flatPrice = flatPrice;
	}

	public StandardTariffPrices getParkingPrice() {
		return parkingPrice;
	}

	public void setParkingPrice(StandardTariffPrices parkingPrice) {
		this.parkingPrice = parkingPrice;
	}

	public AdditionalTariffPrices getRateRider() {
		return rateRider;
	}

	public void setRateRider(AdditionalTariffPrices rateRider) {
		this.rateRider = rateRider;
	}

	public AdditionalTariffPrices getIdleCharge() {
		return idleCharge;
	}

	public void setIdleCharge(AdditionalTariffPrices idleCharge) {
		this.idleCharge = idleCharge;
	}

	public AdditionalTariffPrices getTAX() {
		return TAX;
	}

	public void setTAX(AdditionalTariffPrices tAX) {
		TAX = tAX;
	}

	@Override
	public String toString() {
		return "TariffData [tariffId=" + tariffId + ", maxPriceId=" + maxPriceId + ", minPriceId=" + minPriceId
				+ ", tariffName=" + tariffName + ", startTime=" + startTime + ", endTime=" + endTime + ", energyPrice="
				+ energyPrice + ", timePrice=" + timePrice + ", flatPrice=" + flatPrice + ", parkingPrice="
				+ parkingPrice + ", rateRider=" + rateRider + ", idleCharge=" + idleCharge + ", TAX=" + TAX + "]";
	}
	
}