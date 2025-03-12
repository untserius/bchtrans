package com.evg.ocpp.txns.Service;

import java.util.List;
import java.util.Map;

import com.evg.ocpp.txns.forms.SessionImportedValues;

public interface StatusNotificationService {

//	void sendNotificationForPortStatus(String message, String NoftyType, String stationRefNum, String notificationId,
//			long stationId, long portId, long siteId);

	void updateOcppStatusNotification(String Status, Long stationId, Long connectorId, boolean ampFlag, long siteId,
			boolean ocpiflag);

	List<Map<String, Object>> getPortStatus(long stationId, long connectorId);

	void updatePortStatus(long portUniId, String portStatus);

	void updateStatusApiLoadManagement(String portStatus, long portId, Long stationId, boolean ampFlag);

	void updatingLastUpdatedTime(String newDate, long stationId, long siteId);

	void sendNotificationForPortStatus(String message, String NoftyType, String stationRefNum, String notificationId,
			long stationId, long portId, long siteId, SessionImportedValues siv);

}
