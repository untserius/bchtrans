package com.evg.ocpp.txns.Service;

import java.util.List;
import java.util.Map;

public interface paymentService {

	List<Map<String, Object>> getUserPaymentDetailsBySessionId(String sessionId,String uuid);

	Map<String, Object> getAppConfigData(long orgId, String siteCurrCode);

	//void charge(String stnRefNum, String sessionId, double revenue, String uuid,long accTxnId,String amtDebitType);

	void updateSessionIdUserPayment(String sessionId,String uuid);

	List<Map<String, Object>> getUserPaymentDetailsByUUID(String uuid);

	void capture(String sessionId, double revenue, long accTxnId, String stnRefNum);

	List<Map<String, Object>> getUserPaymentDetailsBySessionId(String sessionId);

	List<Map<String, Object>> getUserPaymentDetailsByIdTag(String idTag);

	void updateSessionIdUserPayment(String sessionId, long userPaymentId);

	boolean authValidationByUserId(long userId);

	boolean authValidation(String idTag);

}
