package com.evg.ocpp.txns.Service;

import java.util.Map;

public interface configService {

	Map<String, Object> getAlertConfigs();

	Map<String, Object> configData(long orgId);

}
