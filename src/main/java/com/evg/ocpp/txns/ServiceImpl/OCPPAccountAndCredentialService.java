package com.evg.ocpp.txns.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.Accounts;
import com.evg.ocpp.txns.model.ocpp.Credentials;
import com.evg.ocpp.txns.model.ocpp.DeviceDetails;
import com.evg.ocpp.txns.model.ocpp.User;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;



@Service
public class OCPPAccountAndCredentialService {
	
	private final static Logger logger = LoggerFactory.getLogger(OCPPAccountAndCredentialService.class);

	@Autowired
	private  GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private LoggerUtil customLogger;
	
	@Autowired
	private Utils utils;
	
	@Value("${mobileServerUrl}")
	protected String mobileServerUrl;
	
	@Value("${mobileAuthKey}")
	private String mobileAuthKey;
	
	
	public List<Map<String,Object>> getCreditCardTxnDetails(long stnId,long portId,String paymentCode){
		List<Map<String, Object>> list = null;
		try {
			String query = "SELECT pgTransactionId,transactionId,dateTime from CreditCardWorldPayRes where stationId = "+ "'"+stnId+"' and portId = '"+portId+"' and paymentCode = '"+paymentCode+"' order by id desc";
			list = executeRepository.findAll(query);
		}catch (Exception e) {
			logger.error("",e);
		}
		return list;
	}

	public  Accounts getAccountById(Long id)  {
		Accounts findById = null;
		try {
			findById = generalDao.findById(new Accounts(), id);
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("",e);
		}
		return findById;
	}
	
	public  User getUser(long userId)  {
		User findById = null;
	 try {
		 findById = generalDao.findById(new User(), userId);
	 }catch (Exception e) {
		// TODO: handle exception
		 logger.error("",e);
	 }
	 return findById;
	}
	

	//Not using Right Now if we don't wont delete this implementation
	public  String getFreeVendorByRFID(String idTag)  {
		String recordBySql = null;
		try {
			recordBySql = generalDao.getRecordBySql("select freevenRFID From free_vendor Where freevenRFID='" + idTag + "'");
		}catch (Exception e) {
			logger.error("",e);
		}
		return recordBySql;
	}
	
	public  String transactionMode(String idTag)  {
		String recordBySql = null;
		try {
			String getTransactionQuery= "select CASE WHEN (select count(*) from free_vendor where freevenRFID='"+idTag+"') >0 THEN  'freeVenRfidTransaction' else (CASE WHEN (select count(*) from CreditCard where rfId='"+idTag+"')>0 THEN 'creditCardTransaction' else 'NormalTransaction' END) END as transactionMode";
			
			recordBySql = generalDao.getRecordBySql(getTransactionQuery);
		}catch (Exception e) {
			logger.error("",e);
		}
		return recordBySql;
	}
	
	public Double getGuestUserBalance(String phoneNumber,Long stationId) {
		double parseDouble = 0.00;
		try {
			String hqlQuery="select cast(isnull(authorizeAmount,0) as string) from PreAuthorizeAmountStripe where phone='"+phoneNumber+"' and stationId="+stationId+" and flagValue='1' order by id desc";
			parseDouble = Double.parseDouble(generalDao.getSingleRecord(hqlQuery));
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("",e);
		}
		return parseDouble;
		
	}
	public Double getNormalUserBalance(Long userId,String stnRefNum) {
		double parseDouble = 0.00;
		try {
			String hqlQuery="select ISNULL(accountBalance,0) as accountBalance from Accounts ac inner join Users u on ac.user_id = u.userId where u.userId='"+userId+"'";
			logger.info("start getNormalUserBalance hqlQuery : "+hqlQuery);
			Map<String, Object> data = executeRepository.findMap(hqlQuery);
			if(data != null && !data.isEmpty()) {
				parseDouble = Double.valueOf(String.valueOf(data.get("accountBalance")));
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return parseDouble;
	}
	public List<DeviceDetails> getDeviceByUser(Long userId)  {
		List<DeviceDetails> findAll = null;
		try {
			findAll = generalDao.findAll("From DeviceDetails Where userId=" + userId, new DeviceDetails());
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
	public boolean getPushNotificationFlag(Long userId) {
		Boolean val = false;
		try {
			val = generalDao.getFlag("select flag from PushNotifications where userId="+userId+"");
		}catch (Exception e) {
			// TODO: handle exception
			logger.error("",e);
		}
		return val;		
	}
	public void updatePreAUthFlag(long id) {
		try {
			String query = "update userPayment set flag=0 where id="+id;
			executeRepository.update(query);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	public List<Map<String, Object>> getUserPaymentDetailsBySessionId(String sessionId,String uuid) {
		List<Map<String, Object>> findAll=null;
		try {
			findAll = executeRepository.findAll("select id from stripeCharge where sessionId='"+sessionId+"' and uuid = '"+uuid+"'");
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
}
