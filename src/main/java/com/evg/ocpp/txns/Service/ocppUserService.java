package com.evg.ocpp.txns.Service;

import java.util.List;
import java.util.Map;

import com.evg.ocpp.txns.model.ocpp.DeviceDetails;

public interface ocppUserService {

	Map<String, Object> getUserDataByUserId(long userId);

	Map<String, Object> getOrgData(long orgId, String stationRefNum);

	List<DeviceDetails> getDeviceDetailsByUserId(Long userId);

	Map<String, Object> getUserDataBySessionIdPayg(String sessionId);

	String getRfidByRfidHex(String fridHex);

	void updateNotification(String column, boolean flag, String sessionId,long resend);

}
