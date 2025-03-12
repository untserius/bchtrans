package com.evg.ocpp.txns.forms;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.evg.ocpp.txns.utils.Utils;

public class MeterValue {

	private List<SampledValue> sampledValue = new ArrayList<SampledValue>();

	private Date  timestamp;

	public List<SampledValue> getSampledValue() {
		return sampledValue;
	}

	public void setSampledValue(List<SampledValue> sampledValue) {
		this.sampledValue = sampledValue;
	}

	public Date getTimestamp() throws ParseException {
		if(timestamp==null)
		return Utils.getUTCDate();
		
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		return "MeterValue [sampledValue=" + sampledValue + ", timestamp=" + timestamp + "]";
	}

}