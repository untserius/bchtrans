package com.evg.ocpp.txns.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.repository.ExecuteRepository;

@Service
public class configServiceImpl implements configService{
	
	@Autowired
	private ExecuteRepository executeRepository;

	@Override
	public Map<String,Object> getAlertConfigs(){
		Map<String,Object> map = new HashMap<>();
		try{
			String query = "select address,email as email_auth,fromEmail as email,host,legacykey,cg.orgId,orgName,password,phoneNumber,port,portalLink,"
					+ " protocol,isnull(serverKey,'') as serverKey,li.url as logo_url,supportEmail from configurationSettings  cg inner join "
					+ " logo_image li on cg.orgId = li.orgId where cg.orgId=1 and li.logoType='main'";
			map = executeRepository.findMap(query);
			if(map != null && !map.isEmpty()) {
			}else {
				query = "select address,email as email_auth,fromEmail as email,host,legacykey,cg.orgId,orgName,password,phoneNumber,port,portalLink,"
						+ " protocol,isnull(serverKey,'') as serverKey,li.url as logo_url,supportEmail from configurationSettings  cg inner join "
						+ " logo_image li on cg.orgId = li.orgId where cg.orgId="+1+" and li.logoType='main'";
				map = executeRepository.findMap(query);
				if(map != null && !map.isEmpty()) {
					
				}else {
					map.put("orgName", "BC Hydro");
					map.put("orgId", "1");
					map.put("legacykey", "");
					map.put("serverKey", "");
					map.put("email", "");
					map.put("protocol", "");
					map.put("phoneNumber", "");
					map.put("portalLink", "");
					map.put("supportEmail", "evsupport@bchydro.com");
					map.put("host", "");
					map.put("port", "");
					map.put("password", "");
					map.put("logo_url", "");
					map.put("address", "333 Dunsmuir St. Vancouver, BC, V6B 5R3, CANADA.");
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	
	@Override
	public Map<String,Object> configData(long orgId) {
		Map<String,Object> map = new HashMap<>();
		List<Map<String,Object>> list = new ArrayList<>();
		try{
			String query = "select ISNULL(address,'5251 California Ave, STE 150, Irvine, CA - 92617.') as address,email,host,legacykey,logoName,"
					+ " orgId,ISNULL(orgName,'BC Hydro') as orgName,password,isnull(phoneNumber,'949-945-2000') as phoneNumber,port,portalLink, "
					+ " isnull(protocol,'smtp') as protocol,isnull(serverKey,'') as serverKey from configurationSettings where orgId = '"+orgId+"'";
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
			e.printStackTrace();
		}
		return map;
	}
}
