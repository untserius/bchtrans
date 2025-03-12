package com.evg.ocpp.txns.ServiceImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.StatusNotificationService;
import com.evg.ocpp.txns.Service.configService;
import com.evg.ocpp.txns.Service.ocpiService;
import com.evg.ocpp.txns.controller.MessageController;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.PushNotification;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class StatusNotificationServiceImpl implements  StatusNotificationService{
	
	private final static Logger logger = LoggerFactory.getLogger(StatusNotificationServiceImpl.class);
	
	@Autowired
	private OCPPDeviceDetailsService ocppDeviceDetailsService;
	
	@Autowired
	private configService configService;
	
	@Autowired
	public PushNotification pushNotification;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private ocpiService ocpiService;
	
	@Value("${LOADMANAGEMENT_URL}")
	private String loadManagementURL;
	
	private final static Logger LOGGER = LoggerFactory.getLogger(StatusNotificationServiceImpl.class);
	
	@Override
    public void updateOcppStatusNotification(String Status, Long stationId, Long connectorId,boolean ampFlag,long siteId,boolean ocpiflag) {
		try {
        	List<Map<String,Object>> stationStatusDB = getPortStatus(stationId, connectorId);
            String updateOcppstatusNotification = "update statusNotification set status='" + Status + "' where StationID=" + stationId + " and port_id =" + connectorId;
        	executeRepository.update(updateOcppstatusNotification);
        	if(ampFlag) {
        		 updateStatusApiLoadManagement(Status, connectorId, stationId, ampFlag);
        	}
           if(stationStatusDB.size() > 0 &&  !String.valueOf(stationStatusDB.get(0).get("status")).equalsIgnoreCase(Status)) {
                String utcDateFormate = Utils.getUTCDateString();
                updatingLastUpdatedTime(utcDateFormate, stationId, siteId);
                ocpiService.postlastupdated(connectorId,ocpiflag);
            }
           updatePortStatus(connectorId,Status);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
	@Override
	public void updatingLastUpdatedTime(String newDate,long stationId,long siteId) {
		try {
			String updateSql = "update station set lastUpdatedDate = '"+newDate+"' where id = "+stationId+";";
			
			updateSql += "update site set lastUpdatedDate = '"+newDate+"' where siteId = "+siteId+";";
			
			updateSql += "update port set lastUpdatedDate = '"+newDate+"' where station_id = "+stationId+";";
			executeRepository.update(updateSql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void updateStatusApiLoadManagement(String portStatus, long portId, Long stationId,boolean ampFlag) {
		try {
			if(ampFlag) {
				Thread th = new Thread() {
					public void run() {
						try {
							String jsonInputString = "{\"portStatus\":\"" + portStatus + "\"," + "\"portId\": "+ portId +"}";
							String URL = loadManagementURL+"/"+"portStatus";
							LOGGER.info("URL : " + URL);
							CloseableHttpClient client = HttpClients.createDefault();
							HttpPost httpPost = new HttpPost(URL);
							StringEntity entity = new StringEntity(jsonInputString);
							httpPost.setEntity(entity);
							httpPost.setHeader("Accept", "application/json");
							httpPost.setHeader("Content-type", "application/json");
							CloseableHttpResponse response = client.execute(httpPost);
							LOGGER.info(" OCPP URL: " + response.getStatusLine());
							client.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				th.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public List<Map<String, Object>> getPortStatus(long stationId, long connectorId)  {
		List<Map<String, Object>> stationStatus = new ArrayList();
		try {
			stationStatus = executeRepository.findAll("select status,timeStamp,requestId From statusNotification where port_id =" + connectorId + " AND stationId=" + stationId + " order by id desc");
		}catch (Exception e) {
			e.printStackTrace();
		}
		return stationStatus;
	}
	@Override
	public void updatePortStatus(long portUniId,String portStatus) {
		try {
			Thread th = new Thread() {
				public void run() {
					try {
						String sql="update port set status = '"+portStatus+"' where id = "+portUniId;
						executeRepository.update(sql);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			th.start();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void sendNotificationForPortStatus(String message, String NoftyType, String stationRefNum, String notificationId, long stationId, long portId,long siteId,SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				Thread thread = new Thread() {
					public void run() {
						try {
							long userId = 0;
							if (siv.getUserObj() != null && !String.valueOf(siv.getUserObj().get("UserId")).equalsIgnoreCase("null")) {
								userId = siv.getUserObj().get("UserId").asLong();
							}
							List<Map<String, Object>> deviceDetails = ocppDeviceDetailsService.getDeviceDetailsByStation(stationId, userId);
							List<String> deviceTokens = new JSONArray();
							List userIds = new ArrayList();
							if (deviceDetails != null) {
								Map<String, Object> orgData = configService.configData(1);
								deviceDetails.forEach(device -> {
									if (String.valueOf(device.get("deviceType")).equalsIgnoreCase("Android")) {
										if (!String.valueOf(device.get("deviceToken")).equalsIgnoreCase("")) {
											deviceTokens.add(String.valueOf(device.get("deviceToken")));
										}
										userIds.add(device.get("userId"));
									}
									if (String.valueOf(device.get("deviceType")).equalsIgnoreCase("iOS")) {
//									iOSRecipients.add(String.valueOf(device.get("deviceToken")));
										userIds.add(device.get("userId"));
									}
								});
								JSONObject info = new JSONObject();
								JSONObject extra = new JSONObject();
								extra.put("stationId", String.valueOf(stationId));
								extra.put("stationName", stationRefNum);
								extra.put("portId", String.valueOf(portId));
								extra.put("siteId", String.valueOf(siteId));
								info.put("sound", "default");
								info.put("action", NoftyType);
								info.put("notificationId", notificationId);
								info.put("extra", String.valueOf(extra));
								info.put("title", String.valueOf(orgData.get("orgName")));
								info.put("body", message);
								info.put("userId", "0");
								if (deviceTokens.size() > 0) {
									pushNotification.sendMulticastMessage(info, deviceTokens, null, 0);
								}
							}
							if (siv.isCurrentScreen()) {
								ocppDeviceDetailsService.deleteDeviceDetails(stationId, userId);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				//customLogger.info(stationRefNum, "pushnotification is called : " );
				thread.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
