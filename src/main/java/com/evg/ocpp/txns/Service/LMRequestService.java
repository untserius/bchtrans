package com.evg.ocpp.txns.Service;

import java.util.Map;

import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;

public interface LMRequestService {
	
	void sendingRequestsToLM(String requestType, String jsonMessage, SessionImportedValues siv, String timeStamp,
			long connectorId);
	
	void deleteIndividualScheduleTime(long portId);

	void updateLMIndividualScheduleTime(long portId);

	void sendInvalidMeterValuesToLM(String jsonMessage, SessionImportedValues siv, MeterValues meterValuesObj,
			FinalData finalData, String stationRefNum, OCPPStartTransaction startTxnObj);

	Map<String, Object> getFleetDataByTransactionId(String requestMessage, SessionImportedValues siv, StopTransaction stopTxnObj);
	
	
}
