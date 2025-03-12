package com.evg.ocpp.txns.Service;

import java.util.Map;

import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;

public interface OCPPActiveTrasactionService {

	OCPPTransactionData getOCPPActiveTransactionData(Long transactionId, long stationReferNo, long connectorId);

	void updateActiveTransaction(String sessionId, boolean flag, long connectorId);

	long getTariffDetails(long portId, String connectorType, long siteId);

}
