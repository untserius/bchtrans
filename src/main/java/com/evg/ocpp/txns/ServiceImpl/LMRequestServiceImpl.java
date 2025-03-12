package com.evg.ocpp.txns.ServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.evg.ocpp.txns.Service.LMRequestService;
import com.evg.ocpp.txns.Service.StatusNotificationService;
import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.MeterValue;
import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.PayloadData;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.Utils;
import com.evg.ocpp.txns.webSocket.MeterValueReadings;

@Service
public class LMRequestServiceImpl implements LMRequestService {

	@Value("${LOADMANAGEMENT_URL}")
	private String loadManagementURL;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ExecuteRepository executeRepository;

	@Autowired
	private MeterValueReadings meterValueReadings;

	@Autowired
	private StatusNotificationService statusNotificationService;

	@Autowired
	private Utils utils;

	private final static Logger logger = LoggerFactory.getLogger(LMRequestServiceImpl.class);

	@Override
	public void sendingRequestsToLM(String requestType, String jsonMessage, SessionImportedValues siv, String timeStamp, long connectorId) {
		if(siv.isAmpFlag()) {
			Thread th = new Thread() {
				public void run() {
					try {
						String status = "Charging";
						if(requestType.equalsIgnoreCase("StopTransaction")) {
							status = "Removed";
						}
						PayloadData lmRequest = new PayloadData();
						lmRequest.setRequestType(requestType);
						lmRequest.setJsonMessage(jsonMessage);
						lmRequest.setStationId(siv.getStnId());
						lmRequest.setPortId(siv.getPortId());
						lmRequest.setSessionId(siv.getSession()==null?"":siv.getSession().getSessionId());
						lmRequest.setRefSessionId(siv.getChargeSessId());
						lmRequest.setStartTime(timeStamp);
						lmRequest.setTransactionId(Long.valueOf(siv.getTransactionId()));
						lmRequest.setIdTag(siv.getIdTag()==null?"":siv.getIdTag());
						lmRequest.setConnectorId(connectorId);
						lmRequest.setSocValue(Double.valueOf(siv.getSoCValue()));
						lmRequest.setPowerImportValue(Double.valueOf(siv.getPowerActiveImportValue()));
						lmRequest.setPowerImportUnit("W");
						lmRequest.setPortStatus(status);
						lmRequest.setPowerImportAvg(getCalculatedCurrentImportValue(siv));
						lmRequest.setMeterEndTime(timeStamp);

						CloseableHttpClient client = HttpClients.createDefault();
						String URL = loadManagementURL+"/"+"lmRequest";
						HttpHeaders headers = new HttpHeaders();
						headers.set("Content-Type", "application/json");
						HttpEntity<Object> requestEntity = new HttpEntity<>(lmRequest, headers);
						restTemplate.setRequestFactory(utils.httpConfig(500, 500));
						restTemplate.postForEntity(URL, requestEntity, String.class);
						client.close();
						Thread.sleep(1000);

					} catch (Exception e) {
						logger.error("",e);
					}
				}
			};
			th.start();
		}
	}

	@Override
	public Map<String,Object> getFleetDataByTransactionId(String requestMessage, SessionImportedValues siv, StopTransaction stopTxnObj) {
		Map<String,Object> fs1 = new HashMap<>();
		try {
			String query = "select portId, connectorId from fleet_sessions where transactionId='"+siv.getTransactionId()+"' and status = 'Active'";
			List<Map<String,Object>> fs = executeRepository.findAll(query);
			if(fs.size()>0) {
				fs1 = fs.get(0);
				siv.setAmpFlag(true);
				siv.setPortId(Long.valueOf(String.valueOf(fs1.get("portId"))));
				sendingRequestsToLM("StopTransaction", requestMessage, siv, stopTxnObj.getTimestampStr().replace("T", " ").replace("Z", ""),  Long.valueOf(String.valueOf(fs1.get("connectorId"))));
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		return fs1;
	}

	@Override
	public void deleteIndividualScheduleTime(long portId) {
		try {
			String query = "delete from individual_ScheduleTime where flag= 1 and portId = "+portId;
			executeRepository.update(query);
		} catch (Exception e) {
			logger.error("",e);
		}
	}

	@Override
	public void updateLMIndividualScheduleTime(long portId) {
		try {
			String utcDateTime = Utils.getUTC();
			String query = "Update individual_ScheduleTime set flag = 1 where portId = " +portId+" and startTime<'" + utcDateTime + "'";
			executeRepository.update(query);
		} catch (Exception e) {
			logger.error("",e);
		}

	}

	@Override
	public void sendInvalidMeterValuesToLM(String jsonMessage, SessionImportedValues siv, MeterValues meterValuesObj, FinalData finalData, String stationRefNum,
			OCPPStartTransaction startTxnObj) {
		try {
			
			if(siv.isAmpFlag()) {
				for (MeterValue mValue : meterValuesObj.getMeterValues()) {
					siv.setMeterValueTimeStatmp(mValue.getTimestamp());
					siv = meterValueReadings.MeterValueReadings(finalData,stationRefNum, siv, mValue,startTxnObj);
				}
				String query="select status from statusNotification where port_id="+siv.getPortId()+" and (status='Charging' or status='inuse')";
				List<Map<String,Object>> list=executeRepository.findAll(query);
				
//				statusNotificationService.updateOcppStatusNotification("Charging", siv.getStnId(), siv.getPortId(),true,0,true);
				if(list.size()>0) {
					sendingRequestsToLM("InvalidMeterValues", String.valueOf(jsonMessage), siv, meterValuesObj.getTimestampStr().replace("T", " ").replace("Z", ""), meterValuesObj.getConnectorId());
				}
			}
		}catch (Exception e) {
			logger.error("",e);
		}
	}

	private double getCalculatedCurrentImportValue(SessionImportedValues siv) {
		double v1 = 0.00;
		try {
			double CI1 = Double.valueOf(String.valueOf(siv.getCurrentImportPhase1Value())==null?"0.0":siv.getCurrentImportPhase1Value());
			double CI2 = Double.valueOf(String.valueOf(siv.getCurrentImportPhase2Value())==null?"0.0":siv.getCurrentImportPhase2Value());
			double CI3 = Double.valueOf(String.valueOf(siv.getCurrentImportPhase3Value())==null?"0.0":siv.getCurrentImportPhase3Value());

			double V1 = Double.valueOf(String.valueOf(siv.getVoltagePhase1Value())==null?"0.0":siv.getVoltagePhase1Value());
			double V2 = Double.valueOf(String.valueOf(siv.getVoltagePhase2Value())==null?"0.0":siv.getVoltagePhase2Value());
			double V3 = Double.valueOf(String.valueOf(siv.getVoltagePhase3Value())==null?"0.0":siv.getVoltagePhase3Value());


			//double average_of_CI = (CI1+CI2+CI3)/3;
			double average_of_CI = CI1+CI2+CI3;
			double average_of_V =  (V1+V2+V3)/3;
			double	value =  average_of_V>0?average_of_CI/average_of_V:0.00;
			v1 = utils.round2decimals(value);
		} catch (Exception e) {
			logger.error("",e);
		}
		return v1;
	}
}
