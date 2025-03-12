package com.evg.ocpp.txns.ServiceImpl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.propertiesService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.repository.ExecuteRepository;
@Service
public class propertiesServiceImpl implements propertiesService{

	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	private final static Logger logger = LoggerFactory.getLogger(propertiesServiceImpl.class);
	
	@Override
	public String getPropety(String key) {
		String recordBySql = "";
		try {
			String query = "select value from serverProperties where property = '"+key+"'";
			recordBySql = generalDao.getRecordBySql(query);
			recordBySql = recordBySql == null ? "0" : recordBySql.equalsIgnoreCase("null") ? "0" : recordBySql;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return recordBySql;
	}
	
	@Override
	public Map<String, Object> getPrimaryPropety(long orgId) {
		Map<String, Object> recordBySql = new HashMap();
		try {
			String query = "select primaryEmail,primaryHost,primaryPassword,primaryPort from serverProperties where orgId = "+orgId+"";
			recordBySql = executeRepository.findAll(query).get(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return recordBySql;
	}
	
	@Override
	public Map<String, Object> getSecondaryPropety(long orgId) {
		Map<String, Object> recordBySql = new HashMap();
		try {
			String query = "select secondryEmail,secondryHost,secondryPassword,secondryPort from serverProperties where orgId = "+orgId+"";
			recordBySql = executeRepository.findAll(query).get(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return recordBySql;
	}

}
