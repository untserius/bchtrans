package com.evg.ocpp.txns.Service;

import java.util.List;
import java.util.Map;

import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPITransactionData;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.model.ocpp.session_pricings;

public interface ocppMeterValueService {
	
	public OCPPTransactionData getTxnData(OCPPStartTransaction siv);

	SessionImportedValues touBilling(SessionImportedValues siv);

	public SessionImportedValues insertIntoSession(SessionImportedValues siv);

	public void insertIntoSessionPricings(SessionImportedValues siv);

	SessionImportedValues insertIntoStopSessionData(SessionImportedValues siv);

	session_pricings getSessesionPricing(String sessionId);

	SessionImportedValues amountDeduction(SessionImportedValues siv);

	SessionImportedValues insertIntoAccountTransaction(SessionImportedValues siv);

	SessionImportedValues deleteTxnData(SessionImportedValues siv);

	void sendPushNotification(SessionImportedValues siv, String NoftyType);

	//void updatePayGSession(SessionImportedValues siv);

	Map<String, Object> getPayGBySessionId(String sessionId);

	SessionImportedValues billing(SessionImportedValues siv);

	public void ocpiMeterReadingCall(String valueOf, String stationRefNum,String sessionId);

	void ocpiMeterStopCall(String requestMessage, String stnRefNum,String sessionId,long stnId);

	boolean insertMeterValues(SessionImportedValues siv, FinalData finalData);
	
	SessionImportedValues lowBalanceValidation(SessionImportedValues siv);

	void updateTxnDataRSTPFlag(String sessionId, int val);

	void sessionInterruptAlert(SessionImportedValues siv, StopTransaction stopTxnObj);

	void updateTxnData(OCPPTransactionData TXN);

	Map<String, Object> getPreviousSessionData(String sessionId);

	void idleBilling(String sessionId, String time);

	OCPITransactionData getOCPITxnData(OCPPStartTransaction startTxn);

	void insertNotificationTracker(SessionImportedValues siv);

}
