package com.evg.ocpp.txns.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface DynamicTariffService {

	Map<String, Object> getLastSessionDetails(String randomSessionId);

	void insertSessionPricing(long sessionId, long dynamicPriceId, long dymanicProfileId, double totalKwUsed,
			double sessionElapsedInMinutes, double dynamicCost, String startTransTimeStamp, String endTimeStamp,
			String randomSessionId, double portPrice, String portPriceUnit, double cost, String dynamicProfileName,Map<String,Object> data);

	String getTimeById(long Id);

	double getPreviousCost(String randomSessionId, long dynamicPriceId,boolean flag,boolean flag1);

	List<Map<String, Object>> getDriverGroupProfileDetails(Date startTime, Date currentTime, long driverGroupId,
			long monthId, long currentDayId, String chargerType);

	List<Map<String, Object>> getBillingDetails(List<Map<String, Object>> profileDetailsList, Date startTransactionTime,
			Date currentTime, String randomSessionId, Map<String, Object> portObj, double kwhUsed, double duration,
			double vendingPrice, String vendingUnit);

	List<Map<String, Object>> getSessionData(long sessionId);
	
}
