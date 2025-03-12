package com.evg.ocpp.txns.Service;

import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.model.es.OCPPChargingIntervalData;
import com.evg.ocpp.txns.repository.es.EVGRepositoryChargingInterval;

@Service
public class EVGSearchService {

	@Autowired
	private EVGRepositoryChargingInterval chargingInterval;

	public OCPPChargingIntervalData findbySessionId(String sessionId) throws ParseException {
		OCPPChargingIntervalData repositoryChargingInterval= null;
		try {
		Pageable pageable = PageRequest.of(0, 1, Sort.by("endInterval").descending());

		if (!sessionId.equalsIgnoreCase("null")) {
			Page<OCPPChargingIntervalData> data= chargingInterval.findBySessionId(sessionId ,pageable);
			List<OCPPChargingIntervalData> list = data.getContent();
			if(list.size()>0) {
				repositoryChargingInterval=list.get(0);
			}
		} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		return repositoryChargingInterval;

	}

//	public StationLogs update(String id) {
//		StationLogs<StationLogs> StationLogs = repository.findById(id);
//		ocppdata.setStnRefNum(String stnRefNum);
//
//		return repository.update(ocppdata);
//	}
	
//	public void OCPPdata (String id,OCPPdata ocppdata) {
//		for(int i = 0; i < ocppdata.size();i++) {
//			OCPPdata d = OCPPdata.getId();			
//			
//		}
//	}
	

}
