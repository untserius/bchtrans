package com.evg.ocpp.txns.Service;

import java.util.Map;

public interface sessionDataService {

	Map<String,Object> getSessionData(String sessionId);

}
