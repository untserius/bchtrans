package com.evg.ocpp.txns.Service;

import java.util.Map;

public interface propertiesService {

	public String getPropety(String key) ;

	Map<String, Object> getPrimaryPropety(long orgId);

	Map<String, Object> getSecondaryPropety(long orgId);
}
