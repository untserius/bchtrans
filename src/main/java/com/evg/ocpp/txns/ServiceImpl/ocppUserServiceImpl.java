package com.evg.ocpp.txns.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.ocppUserService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.DeviceDetails;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class ocppUserServiceImpl implements ocppUserService {
	
	private final static Logger logger = LoggerFactory.getLogger(ocppUserServiceImpl.class);
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Autowired
	private LoggerUtil customLogger;
	
	@Autowired
	private Utils utils;
	
	@Override
	public List<DeviceDetails> getDeviceDetailsByUserId(Long userId)  {
		List<DeviceDetails> findAll = null;
		try {
			findAll = generalDao.findAll("From DeviceDetails Where userId=" + userId, new DeviceDetails());
		}catch (Exception e) {
			logger.error("",e);
		}
		return findAll;
	}
	
	@Override
	public Map<String,Object> getUserDataByUserId(long userId){
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select a.id as accid,isnull(a.digitalId,'-') as digitalId,a.accountBalance,a.accountName,a.activeAccount,a.creationDate,a.user_id,isnull(a.currencyType,'USD') as crncy_Code,"
					+ " isnull(a.currencySymbol,'&#36;') as crncy_HexCode,ad.phone as phoneNumber,u.UserId,u.email,isnull(p.zone_id,1) as userTimeZoneId,u.uid as uuid,isNull(u.orgId,'1') as orgId from accounts a inner join profile p "
					+ " on a.user_id = p.user_id inner join  Users u  on a.user_id=u.UserId inner join address ad on u.userId = ad.user_id where u.UserId="+userId;
			map = executeRepository.findMap(str);
			
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}
	@Override
	public String getRfidByRfidHex(String fridHex) {
		String str = null;
		try {
			String str1 = "select case when phone is null then rfid when phone is not null then phone end as phone from creadential where rfidHex='"+fridHex+"' or rfid='"+fridHex+"' or phone = '"+fridHex+"'";
			List<Map<String, Object>> findAll = executeRepository.findAll(str1);
			if(findAll != null && findAll.size() > 0) {
				str = String.valueOf(findAll.get(0).get("phone"));
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return str;
	}
	
	@Override
	public Map<String,Object> getUserDataBySessionIdPayg(String sessionId){
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select email,phone,isnull(currencyType,'USD') as crncy_Code from userPayment where sessionId = '"+sessionId+"'";
			map = executeRepository.findMap(str);
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}
	
	public Map<String,Object> getOrgData(long orgId,String stationRefNum) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		try{
			String query = "select ISNULL(address,'5251 California Ave, STE 150, Irvine, CA - 92617.') as address,email,host,legacykey,logoName,"
					+ " orgId,ISNULL(orgName,'BC Hydro') as orgName,password,isnull(phoneNumber,'949-945-2000') as phoneNumber,port,portalLink, "
					+ " isnull(protocol,'smtp') as protocol,isnull(serverKey,'') as serverKey from configurationSettings where orgId = '1'";
			list = executeRepository.findAll(query);
			if(list.size() > 0) {
				map = list.get(0);
			}else {
				map.put("orgName", "BC Hydro");
				map.put("orgId", "1");
				map.put("legacykey", "");
				map.put("serverKey", "");
				map.put("email", "no-reply@evgateway.net");
				map.put("phoneNumber", "no-reply@evgateway.net");
				map.put("host", "smtp.office365.com");
				map.put("port", "587");
				map.put("password", "EvGateway@1234!");
				map.put("address", "5251 California Ave, STE 150, Irvine, CA - 92617.");
			}
		}catch(Exception e) {
			logger.error("",e);
		}
		return map;
	}
	
	
	public Map<String,Object> getNewOrgDetails(Long oldOrg) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		try{
			String query =  "SELECT id as orgId,OrgName as orgName FROM organization WHERE oldOrgId = '"+oldOrg+"'";
			list = executeRepository.findAll(query);
			if(map == null || map.size() == 0) {
				map = list.get(0);
			}else {
				map.put("orgName", "BC Hydro");
				map.put("orgId", "1");
			}
		}catch(Exception e) {
			logger.error("",e);
		}
		return map;
	}
	public Map<String,Object> getWhiteLabelOrg(Long stnId) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		try{
			String query =  "select distinct o.orgName,o.id as orgId from organization o inner join dealer_in_org do on do.orgId = o.id "
					+ " inner join owner_in_dealer od on od.dealerId = do.dealerId  inner join users_in_sites us on us.userId = "
					+ " od.ownerId inner join site si on us.siteId = si.siteId inner join station st on si.siteId = st.siteId where "
					+ " o. whitelabel = 1 and st.id = '"+stnId+"'";
			list = executeRepository.findAll(query);
			if(list.size() > 0) {
				map = list.get(0);
			}else {
				map.put("orgName", "BC Hydro");
				map.put("orgId", "1");
			}
		}catch(Exception e) {
			logger.error("",e);
		}
		return map;
	}
	@Override
	public void updateNotification(String column,boolean flag,String sessionId,long resend) {
		try {
			String query="update notification_tracker set "+column+"='"+flag+"',resendCount=resendCount+"+resend+",modifiedDate='"+utils.getUTCDateString()+"',resend="+resend+" where sessionId='"+sessionId+"'";
		    executeRepository.update(query);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
}
