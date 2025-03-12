package com.evg.ocpp.txns.ServiceImpl;

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

import com.evg.ocpp.txns.Service.paymentService;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class paymentServiceImpl implements paymentService{
	
	private final static Logger logger = LoggerFactory.getLogger(paymentServiceImpl.class);
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Value("${mobileServerUrl}")
	protected String mobileServerUrl;
	
	@Value("${mobileAuthKey}")
	private String mobileAuthKey;
	
	@Autowired
	private Utils utils;
	
	@Autowired
	private LoggerUtil customLog;
	
	@Override
	public Map<String,Object> getAppConfigData(long orgId,String siteCurrCode){
		Map<String,Object> map = new HashMap<>();
		try {
			String query = "select userProcessingFee,payAsYouGoProcessingFee from app_config_setting where orgId = '"+orgId+"' and currencyCode = '"+siteCurrCode+"'";
			List<Map<String, Object>> findAll = executeRepository.findAll(query);
			if(findAll.size() == 0) {
				map.put("userProcessingFee", "0.5");
				map.put("payAsYouGoProcessingFee", "0.5");
			}else {
				map.put("userProcessingFee", findAll.get(0).get("userProcessingFee"));
				map.put("payAsYouGoProcessingFee", findAll.get(0).get("payAsYouGoProcessingFee"));
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}
	
	@Override
	public void capture(String sessionId, double revenue,long accTxnId,String stnRefNum) {
		try {
			List<Map<String, Object>> userPaymentDetailsBySessionId = getUserPaymentDetailsBySessionId(sessionId);
			customLog.info(stnRefNum, "userPaymentDetailsBySessionId : "+userPaymentDetailsBySessionId);
			if(userPaymentDetailsBySessionId.size() > 0) {
				String userPaymentId = String.valueOf(userPaymentDetailsBySessionId.get(0).get("id"));
				String userPaymentType = String.valueOf(userPaymentDetailsBySessionId.get(0).get("userType"));
				if(revenue >= 0.5) {
					if(String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("Android") || String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("iOS")) {
						String urlToRead = mobileServerUrl+"api/v3/payment/paymentIntent/capture";
						customLog.info(stnRefNum, "Android/iOS pay as you go urlToRead : "+urlToRead);
						Map<String, Object> params = new HashMap<String, Object>(); 
						params.put("userPaymentId", userPaymentId);
						params.put("userType", userPaymentType);
						params.put("captureAmount", revenue);
						params.put("accTxnId", accTxnId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						headers.set("EVG-Correlation-ID", mobileAuthKey);
						HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
						customLog.info(stnRefNum, "payment Type stripe capture request body : "+params);
						utils.apicallingPOST(urlToRead, requestEntity);
					}else if(String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("Web")) {
						String urlToRead = mobileServerUrl+"api/v3/payment/stripe/capture";
						customLog.info(stnRefNum, "Portal/Web Android/iOS pay as you go urlToRead : "+urlToRead);
						Map<String, Object> params = new HashMap<String, Object>(); 
						params.put("userPaymentId", userPaymentId);
						params.put("userType", userPaymentType);
						params.put("captureAmount", revenue);
						params.put("accTxnId", accTxnId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						headers.set("EVG-Correlation-ID", mobileAuthKey);
						HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
						customLog.info(stnRefNum, "payment Type stripe capture request body : "+params);
						utils.apicallingPOST(urlToRead, requestEntity);
					}
				}else if(revenue < 0.5 && !String.valueOf(userPaymentDetailsBySessionId.get(0).get("paymentMode")).equalsIgnoreCase("Freeven")) {
					if(String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("Android") || String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("iOS")) {
						String urlToRead = mobileServerUrl+"api/v3/payment/paymentIntent/cancelAuthorization";
						customLog.info(stnRefNum, "Android/iOS pay as you go urlToRead : "+urlToRead);
						Map<String, Object> params = new HashMap<String, Object>(); 
						params.put("userPaymentId", userPaymentId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						headers.set("EVG-Correlation-ID", mobileAuthKey);
						HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
						customLog.info(stnRefNum, "payment Type stripe cancel authorize request body : "+params);
						utils.apicallingPOST(urlToRead, requestEntity);
					}else if(String.valueOf(userPaymentDetailsBySessionId.get(0).get("deviceType")).equalsIgnoreCase("Web")) {
						String urlToRead = mobileServerUrl+"api/v3/payment/stripe/cancelAuthorization";
						customLog.info(stnRefNum, "Portal/Web Android/iOS pay as you go urlToRead : "+urlToRead);
						Map<String, Object> params = new HashMap<String, Object>(); 
						params.put("userPaymentId", userPaymentId);
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						headers.set("EVG-Correlation-ID", mobileAuthKey);
						HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
						customLog.info(stnRefNum, "payment Type stripe cancel authorize request body : "+params);
						utils.apicallingPOST(urlToRead, requestEntity);
					}
				}
			}
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	@Override
	public List<Map<String, Object>> getUserPaymentDetailsBySessionId(String sessionId) {
		List<Map<String, Object>> findAll=null;
		try {
			findAll = executeRepository.findAll("select id,phone,userType,authorizeAmount,deviceType from userPayment where sessionId='"+sessionId+"' and count < 3 order by id desc");
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
	
	@Override
	public List<Map<String, Object>> getUserPaymentDetailsByIdTag(String idTag) {
		List<Map<String, Object>> findAll=null;
		try {
			findAll = executeRepository.findAll("select id,phone from userPayment where phone='"+idTag+"' and sessionId is null and flag=1 and count < 3 order by id desc");
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
	
	@Override
	public void updateSessionIdUserPayment(String sessionId,long userPaymentId) {
		try {
			executeRepository.update("update userPayment set sessionId='"+sessionId+"' where id="+userPaymentId);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	@Override
	public boolean authValidation(String idTag) {
		boolean flag = false;
		try {
			String qry = "select * from userPayment where phone='"+idTag+"' and flag=1 and sessionId is null and count < 3";
			List<Map<String, Object>> findAll = executeRepository.findAll(qry);
			if(findAll.size() >0) {
				flag = true;
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return flag;
	}

	@Override
	public boolean authValidationByUserId(long userId) {
		boolean flag = false;
		try {
			String qry = "select id from userPayment where userId='"+userId+"' and flag=1 and sessionId is null and count < 3";
			List<Map<String, Object>> findAll = executeRepository.findAll(qry);
			if(findAll.size() >0) {
				flag = true;
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return flag;
	}
	
	@Override
	public List<Map<String, Object>> getUserPaymentDetailsBySessionId(String sessionId,String uuid) {
		List<Map<String, Object>> findAll=null;
		try {
			findAll = executeRepository.findAll("select id from stripeCharge where sessionId='"+sessionId+"' and uuid = '"+uuid+"'");
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
	
	@Override
	public List<Map<String, Object>> getUserPaymentDetailsByUUID(String uuid) {
		List<Map<String, Object>> findAll=null;
		try {
			findAll = executeRepository.findAll("select id from stripeCharge where uuid = '"+uuid+"' and flag=1 order by id desc");
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}


	@Override
	public void updateSessionIdUserPayment(String sessionId,String uuid) {
		try {
			List<Map<String, Object>> userPaymentDetailsByUserId = getUserPaymentDetailsByUUID(uuid);
			if(userPaymentDetailsByUserId.size() > 0) {
				executeRepository.update("update stripeCharge set sessionId='"+sessionId+"' where id='"+String.valueOf(userPaymentDetailsByUserId.get(0).get("id"))+"'");
			}
			
		}catch (Exception e) {
			logger.error("",e);
		}
	}

}
