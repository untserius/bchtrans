package com.evg.ocpp.txns.ServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.StationService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StationServiceImpl  implements StationService{
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private  GeneralDao<?, ?> generalDao;
	
	@Value("${LOADMANAGEMENT_URL}")
	private String loadManagementURL;
	
	private final static Logger logger = LoggerFactory.getLogger(StationServiceImpl.class);
	
	@Override
	public long getDealerOrgIdByStnId(long stnId) {
		long orgId=1;
		List<Map<String,Object>> list = new ArrayList<>();
		try{
			String query = "select distinct o.id as orgId from organization o inner join dealer_in_org do on do.orgId = o.id "
					+ " inner join owner_in_dealer od on od.dealerId = do.dealerId  inner join users_in_sites us on us.userId = "
					+ " od.ownerId inner join site si on us.siteId = si.siteId inner join station st on si.siteId = st.siteId "
					+ " where st.id = '"+stnId+"'";
			list = executeRepository.findAll(query);
			if(list.size() > 0) {
				orgId = Long.valueOf(String.valueOf(list.get(0).get("orgId")));
			}else {
//				String selectQuery = "select distinct o.id as orgId from station st inner join site si on "
//						+ " si.siteId = st.siteId inner join organization o on o.id = si.org where st.id = '"+stnId+"'";
//				list = executeRepository.findAll(selectQuery);
//				if(list.size() > 0) {
//					orgId = Long.valueOf(String.valueOf(list.get(0).get("orgId")));
//				}
				orgId = 1;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return orgId;
	}
	
	@Override
	public Map<String,Object> getPortByRefNum(long stnId,long portId)  {
		Map<String,Object> map = new HashMap<>();
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select id,connector_id,station_id,isnull(chargerType,'AC') as chargerType,isnull(vendingPriceUnit,'Hr') as vendingPriceUnit,isnull(displayName,'Port-1') as displayName,isnull(vendingPriceUnit1,'Hr') as vendingPriceUnit1,isnull(vendingPriceUnit2,'kWh') as vendingPriceUnit2,capacity,(select displayName from connectorType where id = ISNULL(standard,1)) as connectorType,isnull(power_type,1) as power_type,advPricing,ISNULL(postTimeLimitPrice,'0') AS postTimeLimitPrice, ISNULL((select (t.hour * 60) + (t.min) + (t.sec / 60) from time t where id = p.vendingPriceTimelimit),0) as vendingPriceTimelimit, p.Amperage,p.capacity,p.uuid,p.format,p.power_type,p.standard  from port p  where station_id = '"+stnId+"' and connector_id = '"+portId+"'");
			
			if(mapData.size() > 0) {
				map = mapData.get(0);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	@Override
	public String getStnObjByRefNos(String stnRefNo,long connectorId){
		ObjectMapper objectMapper = new ObjectMapper();
		String str = "{}";
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select st.id as stnId,p.id as portId,isnull(st.ampFlag,'0') as ampFlag from station st inner join manufacturer m "
					+ " on st.manufacturerId = m.id inner join port p on p.station_Id = st.id where st.referNo = '"+stnRefNo+"' and p.connector_id='"+connectorId+"'");
			if(mapData.size() > 0) {
				str = objectMapper.writeValueAsString(mapData.get(0));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return str;
	}
	@Override
	public Map<String,Object> getStnObjByUniIds(long portId){
		Map<String,Object> map = new HashMap<>();
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select st.id as stnId,st.referNo as stnRefNum,isNull(st.supportMailFlag,'0') as supportMailFlag,"
					+ " ISNULL(CASE when (st.stationMode = 'null') Then 'paymentMode' ELSE st.stationMode END,'freeven') as stationMode,"
					+ " isnull(st.stationMaxSessiontime,'0') as stationMaxSessiontime,"
					+ " ISNULL(st.exceedingHours,'0') as exceedingHours, ISNULL(st.exceedingMints,'0') as exceedingMints,Replace(ISNULL(st.stationAddress,'-'),'null','') as stationAddress,"
					+ " st.autocharging,st.preProduction,isnull(st.mailEnable,'Disable') as mailEnable,isnull(st.portQuantity,0) as portQuantity,st.stationName,"
					+ " m.id as manfId,m.manfname,st.creationDate,st.ampFlag as ampFlag,st.capacity,ISNULL(st.siteId,0) as siteId,p.id as portId,connector_id,station_id,"
					+ " isnull(chargerType,'AC') as chargerType,isnull(displayName,'Port-1') as displayName,"
					+ " (select displayName from connectorType where id = ISNULL(standard,1)) as connectorType,isnull(power_type,1) as power_type,"
					+ "  p.Amperage,p.capacity as portCapacity,p.uuid,p.format,p.power_type,p.standard "
					+ " from station st inner join manufacturer m on st.manufacturerId = m.id inner join port p on p.station_Id = st.id where p.id='"+portId+"'");
			if(mapData.size() > 0) {
				map = mapData.get(0);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	@Override
	public Map<String,Object> getStnByRefNum(String stationRefNo)  {
		Map<String,Object> map = new HashMap<>();
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select st.id as stnId,st.referNo,isNull(st.supportMailFlag,'0') as supportMailFlag,"
					+ " ISNULL(CASE when (st.stationMode = 'null') Then 'Normal' ELSE st.stationMode END,'freeven') as stationMode, isnull(st.stationFreeDuration,'0') as "
					+ " stationFreeDuration,isnull(st.greetingMail,'') as greetingMail ,isnull(st.greetingMessage,'') as greetingMessage,isnull(st.graceTime,'0') as graceTime ,"
					+ " isnull(st.connectedTime,'0') as connectedTime,isnull(st.connectedTimeFlag,'0') as connectedTimeFlag, isnull(st.connectedTimeUnits,'Hr') as "
					+ " connectedTimeUnits, isnull(st.stationMaxSessiontime,'0') as stationMaxSessiontime,ISNULL(st.exceedingHours,'0') as exceedingHours,"
					+ " ISNULL(st.exceedingMints,'0') as exceedingMints,ISNULL(st.stationAddress,'-') as stationAddress,st.autocharging,st.preProduction,"
					+ " isnull(st.mailEnable,'Disable') as mailEnable,isnull(st.portQuantity,0) as portQuantity,st.stationName,m.id as manfId,m.manfname,"
					+ " st.creationDate,st.ampFlag as ampFlag,st.capacity,ISNULL(st.siteId,0) as siteId from "
					+ " station st inner join manufacturer m on st.manufacturerId = m.id where st.referNo = '"+stationRefNo+"'");
			if(mapData.size() > 0) {
				map = mapData.get(0);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	@Override
	public long getStationConnectorId(long connectorId) {
		long connectorNum = 1;
		try {
			String queryforConnectorDisplay="select ISNULL(connector_id,'1') AS connector_id from port where id="+connectorId+"";
			String connId = String.valueOf(executeRepository.findAll(queryforConnectorDisplay).get(0).get("connector_id"));
			
			if(connId!=null) {
				connectorNum = connId.contains("1") ? 1 :connId.contains("2") ? 2 :connId.contains("3") ? 3 : 1;
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return connectorNum;
	}
	
	@Override
	public Map<String,Object> getCoordinatesByCoordinateIdId(long coordinateId){
		Map<String, Object> map = new HashMap<>();
        try {
            String str = "select latitude,longitude from geoLocation where id ="+coordinateId;
            List<Map<String, Object>> mapData = executeRepository.findAll(str);
            if(mapData.size() > 0) {
                map = mapData.get(0);
            }else {
            	map.put("ocpiflag", "0");
                map.put("currencySymbol", "&#36;");
                map.put("currencyType", "USD");
                map.put("siteId", "0");
                map.put("processingFee", "0.00");
                map.put("siteName", "-");
                map.put("saleTexPerc", "0.00");
                map.put("uuid", "0");
                map.put("streetName", "");
                map.put("city", "");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}
	@Override
	public Map<String,Object> getPortFormatsByPortId(long formatId){
		Map<String, Object> map = new HashMap<>();
        try {
            String str = "select name from connectorFormat where id="+formatId;
            List<Map<String, Object>> mapData = executeRepository.findAll(str);
            if(mapData.size() > 0) {
                map = mapData.get(0);
            }else {
            	map.put("name", "CABLE");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}
	
	@Override
	public Map<String,Object> getPortPowersByPortId(long formatId){
		Map<String, Object> map = new HashMap<>();
        try {
            String str = "select name from powerType where id="+formatId;
            List<Map<String, Object>> mapData = executeRepository.findAll(str);
            if(mapData.size() > 0) {
                map = mapData.get(0);
            }else {
            	map.put("name", "CABLE");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}
	
	@Override
	public Map<String,Object> getPortStandardByPortId(long formatId){
		Map<String, Object> map = new HashMap<>();
        try {
            String str = "select name from connectorType where id="+formatId;
            List<Map<String, Object>> mapData = executeRepository.findAll(str);
            if(mapData.size() > 0) {
                map = mapData.get(0);
            }else {
            	map.put("name", "CABLE");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}

	
	@Override
	public OCPPStartTransaction getStartTransaction(SessionImportedValues siv) {
		try {
			return generalDao.findOne("FROM OCPPStartTransaction OS WHERE OS.transactionId=" + siv.getTransactionId()
					+ " and OS.connectorId=" + siv.getPortId() + " and OS.stationId=" + siv.getStnId()
					+ "  and OS.transactionStatus='Accepted' order by OS.id desc", new OCPPStartTransaction());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	 
	@Override
	public OCPPStartTransaction getStartTransactionWithRfid(SessionImportedValues siv)  {
		try {
			return generalDao.findOneBySQLQuery("select * from ocpp_startTransaction where transactionId="+siv.getTransactionId()+" and stationId="+siv.getStnId()+" order by id desc" , new OCPPStartTransaction());
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	 public Map<String, Object> getSiteDetails(long stationId) {
        Map<String, Object> map = new HashMap<>();
        try {
        	String str = "select isnull(si.currencySymbol,'&#36;') as crncy_HexCode,si.timeZone,si.streetName,si.city,si.state,si.postal_code,ISNULL(si.currencyType,'USD') as crncy_Code, isNull(si.siteId,'0') as siteId,si.siteName as siteName, isnull(si.uuid,'0') as uuid,si.ocpiflag,si.coordinateId,si.country  from station st inner join site si on st.siteId = si.siteId where st.id = '"+stationId+"'";
            List<Map<String, Object>> mapData = executeRepository.findAll(str);
            if(mapData.size() > 0) {
                map = mapData.get(0);
            }else {
            	map.put("ocpiflag", "0");
                map.put("crncy_HexCode", "&#36;");
                map.put("crncy_Code", "USD");
                map.put("siteId", "0");
                map.put("siteName", "-");
                map.put("uuid", "0");
                map.put("streetName", "");
                map.put("city", "");
                map.put("country", "");
                map.put("timeZone", "PDT");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return map;
	}

	@Override
	public Map<String, Object> getStnObjByRefNosMap(String stnRefNo, long connectorId) {
		Map<String,Object> map = new HashMap<>();
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select st.id as stnId,st.referNo as stnRefNum,isNull(st.supportMailFlag,'0') as supportMailFlag,"
					+ " ISNULL(CASE when (st.stationMode = 'null') Then 'paymentMode' ELSE st.stationMode END,'freeven') as stationMode,"
					+ " isnull(st.stationMaxSessiontime,'0') as stationMaxSessiontime,"
					+ " ISNULL(st.exceedingHours,'0') as exceedingHours, ISNULL(st.exceedingMints,'0') as exceedingMints,Replace(ISNULL(st.stationAddress,'-'),'null','') as stationAddress,"
					+ " st.autocharging,st.preProduction,isnull(st.mailEnable,'Disable') as mailEnable,isnull(st.portQuantity,0) as portQuantity,st.stationName,"
					+ " m.id as manfId,m.manfname,st.creationDate,st.ampFlag as ampFlag,st.capacity,ISNULL(st.siteId,0) as siteId,p.id as portId,connector_id,station_id,"
					+ " isnull(chargerType,'AC') as chargerType,isnull(displayName,'Port-1') as displayName,"
					+ " (select displayName from connectorType where id = ISNULL(standard,1)) as connectorType,isnull(power_type,1) as power_type,"
					+ "  p.Amperage,p.capacity as portCapacity,p.uuid,p.format,p.power_type,p.standard "
					+ " from station st inner join manufacturer m on st.manufacturerId = m.id inner join port p on p.station_Id = st.id where st.referNo = '"+stnRefNo+"' "
							+ " and p.connector_id='"+connectorId+"'");
			if(mapData.size() > 0) {
				map = mapData.get(0);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public boolean getAmpFlagByStnId(long stnId) {
		boolean b= false;
		try {
			List<Map<String, Object>> mapData = executeRepository.findAll("select id from station where id = "+stnId+" and ampFlag = 1");
			if(mapData.size()>0) {
				b= true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return b;
	}

}
