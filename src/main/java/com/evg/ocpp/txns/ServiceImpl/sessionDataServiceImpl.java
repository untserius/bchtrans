package com.evg.ocpp.txns.ServiceImpl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.alertsService;
import com.evg.ocpp.txns.Service.sessionDataService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPPSessionsData;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.Utils;


@Service
public class sessionDataServiceImpl implements sessionDataService{
	private final static Logger logger = LoggerFactory.getLogger(sessionDataServiceImpl.class);
	
	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private propertiesServiceImpl propertiesServiceImpl;
	
	@Override
	public Map<String,Object> getSessionData(String sessionId){
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select se.rewardType,ISNULL(se.rewardValue,'0') as rewardValue,se.sessionId as sessionId,se.id as sessId,emailId,ISNULL(se.customerId,'-') as customerId,userId,p.id as portId,st.id as stnId,se.finalCostInSlcCurrency as finalAmount,se.cost as chargingCost,"
					+ " sp.cost_Info,st.referNo as stnRefNum,st.stationName,p.connector_id,se.sessionElapsedInMin as sessionDuration,se.startTimeStamp,txnType,se.endTimeStamp,se.socStartVal,se.socEndVal,se.kilowattHoursUsed as totalkWh from session se inner join port p on p.id = se.port_Id inner join station st on p.station_Id = st.id inner join "
					+ " session_pricings sp on se.sessionId = sp.sessionId where se.sessionId='"+sessionId+"'";
			map = executeRepository.findMap(str);
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return map;
	}

}
