package com.evg.ocpp.txns.ServiceImpl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.OCPPActiveTrasactionService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.repository.ExecuteRepository;

@Service
public class OCPPActiveTrasactionServiceImpl implements OCPPActiveTrasactionService{
	
	private final static Logger logger = LoggerFactory.getLogger(OCPPActiveTrasactionServiceImpl.class);
	
	@Autowired
	private  GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private OCPPMeterValueServiceImpl ocppMeterValueService;
	
	@Override
	public long getTariffDetails(long portId,String connectorType,long siteId){
		long tariffId=0;
		try {
			String query="select pt.tariffId from port_in_tariff pt inner join site_in_tariff st on st.tariffId=pt.tariffId inner join tariff t on t.id=st.tariffId where pt.portId='"+portId+"' and st.siteId='"+siteId+"' and (st.connectorType='"+connectorType+"' or st.connectorType='All' ) order by st.id desc";
			List<Map<String,Object>> list= generalDao.getMapData(query);
			if(list.size()>0) {
				tariffId=Long.parseLong(String.valueOf(list.get(0).get("tariffId")));
			}
		}catch(Exception e) {
			
		}
		return tariffId;
	}
	

	@Override
	public OCPPTransactionData getOCPPActiveTransactionData(Long transactionId,long stationId,long connectorId) {
		OCPPTransactionData ocppTransactionData=null;
		try {
			ocppTransactionData= generalDao.findOne("FROM OCPPTransactionData Where stationId='" + stationId + "' and portId='"+connectorId+"' and transactionId = '"+transactionId+"' order by id desc",
					new OCPPTransactionData());
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ocppTransactionData;
	}
	@Override
	public void updateActiveTransaction(String sessionId,boolean flag,long connectorId) {
		try {
			String query="update ocpp_TransactionData set flag='"+flag+"' where sessionId='"+sessionId+"' and portId='"+connectorId+"'";
			executeRepository.update(query);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
