package com.evg.ocpp.txns.Service;

import java.util.Map;

import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.model.ocpp.PreferredNotification;

public interface alertsService {

	SessionImportedValues notifyChargingSessionSummaryData(SessionImportedValues siv);

	PreferredNotification preferredNotify(long userId);

	void lowBalanceMailAlert(SessionImportedValues siv,Double accBal);
	
	void lowBalancePushNotiAlert(SessionImportedValues siv);

	void sessionInterruptedMailAlert(SessionImportedValues siv);

	void sessionInterruptedPushNotifyAlert(SessionImportedValues siv);

	SessionImportedValues smsChargingSessionSummaryData(SessionImportedValues siv);

	void smsSessionInterruptedAlert(SessionImportedValues siv);

	void smsLowBalanceAlert(SessionImportedValues siv);

	void alertPAYGStop(SessionImportedValues siv);

	void notifyInActiveBilling(SessionImportedValues siv);

	void mailInActiveBilling(SessionImportedValues siv);

	SessionImportedValues chargingActivityMail(SessionImportedValues siv);

	boolean chargingActivityNotification(OCPPTransactionData txnData, long userId, double kwhUsed, double finalCost,
			double duration);

	String mailChargingSessionSummaryData(String sessionId, boolean idle);

	String notification(String sessionId, String notifyType);

	boolean chargingSummaryNotification(Map<String, Object> sessionObj, Map<String, Object> userObj, String sessionId);

	void sendMeterValueViolationAlert(SessionImportedValues siv);
}
