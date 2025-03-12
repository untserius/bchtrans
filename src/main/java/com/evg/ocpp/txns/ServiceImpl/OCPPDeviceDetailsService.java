package com.evg.ocpp.txns.ServiceImpl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.DeviceDetails;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.Utils;



@Service
public class OCPPDeviceDetailsService {
	
	private final static Logger logger = LoggerFactory.getLogger(OCPPDeviceDetailsService.class);

	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ExecuteRepository executeRepository;

	public List<DeviceDetails> getDeviceByUser(Long userId)  {
		List<DeviceDetails> findAll = null;
		try {
			findAll = generalDao.findAll("From DeviceDetails Where userId=" + userId, new DeviceDetails());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return findAll;
	}

	public List<Map<String, Object>> getDeviceDetailsByStation(long stationId,long userId){
		List<Map<String, Object>> deviceDetails=null;
		try {
//			String query="select * from currentScreen where stationId ="+stationId+" or siteId ="+siteId;
			String query="select * from currentScreen where screen='Charging Session' and stationId='"+stationId+"' and userId='"+userId+"'";
			deviceDetails=executeRepository.findAll(query);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return deviceDetails;
	}
	public void deleteDeviceDetails(long stationId,long userId) {
		try {
			String query="delete from currentScreen where screen='Charging Session' and stationId='"+stationId+"' and userId='"+userId+"'";
			executeRepository.update(query);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
