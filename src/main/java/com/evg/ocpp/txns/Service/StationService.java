package com.evg.ocpp.txns.Service;

import java.util.List;
import java.util.Map;

import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;

public interface StationService {

	Map<String, Object> getPortByRefNum(long stnId, long portId);

	Map<String, Object> getStnByRefNum(String stationRefNo);

	OCPPStartTransaction getStartTransaction(SessionImportedValues siv);

	Map<String, Object> getCoordinatesByCoordinateIdId(long coordinateId);

	Map<String, Object> getPortStandardByPortId(long formatId);

	Map<String, Object> getPortPowersByPortId(long formatId);

	Map<String, Object> getPortFormatsByPortId(long formatId);

	Map<String, Object> getSiteDetails(long stationId);

	OCPPStartTransaction getStartTransactionWithRfid(SessionImportedValues siv);

	long getStationConnectorId(long connectorId);

	String getStnObjByRefNos(String stnRefNo, long connectorId);

	Map<String,Object> getStnObjByRefNosMap(String stnRefNo, long connectorId);

	long getDealerOrgIdByStnId(long stnId);

	Map<String, Object> getStnObjByUniIds(long portId);

	boolean getAmpFlagByStnId(long stnId);

}
