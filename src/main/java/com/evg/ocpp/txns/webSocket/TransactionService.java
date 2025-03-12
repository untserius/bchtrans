package com.evg.ocpp.txns.webSocket;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.LMRequestService;
import com.evg.ocpp.txns.Service.StationService;
import com.evg.ocpp.txns.Service.StatusNotificationService;
import com.evg.ocpp.txns.Service.alertsService;
import com.evg.ocpp.txns.ServiceImpl.OCPPMeterValueServiceImpl;
import com.evg.ocpp.txns.ServiceImpl.chargingIntervalServiceImpl;
import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPITransactionData;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.utils.EsLoggerUtil;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TransactionService {

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private StationService stationService;

	@Autowired
	private LoggerUtil customLogger;

	@Value("${mobileServerUrl}")
	protected String mobileServerUrl;

	@Value("${mobileAuthKey}")
	private String mobileAuthKey;

	@Autowired
	private OCPPMeterValueServiceImpl ocppMeterValueService;

	@Autowired
	private EsLoggerUtil esLoggerUtil;

	@Autowired
	private Utils utils;

	@Autowired
	private StatusNotificationService statusNotificationService;

	@Autowired
	private alertsService alertsService;

	@Value("${revenue.Alert}")
	protected double revenueAlert;

	@Value("${PushNtfyForEnergyUse}")
	private Integer pushNtfyForEnergyUseCount;

	@Autowired
	private MeterValueReadings meterValueReadings;

	@Autowired
	private LMRequestService lmRequestService;

	@Autowired
	private chargingIntervalServiceImpl chargingIntervalServiceImpl;

	private final static Logger logger = LoggerFactory.getLogger(TransactionService.class);

	public void stopTransaction(FinalData finalData, String stationRefNum, String requestMessage,long stnId) {
		SessionImportedValues siv = new SessionImportedValues();
		StopTransaction stopTxnObj=finalData.getStopTransaction();

		StopTransaction stopTxn = new StopTransaction();
		stopTxn.setTransactionId(stopTxnObj.getTransactionId());
		stopTxn.setMeterStop(stopTxnObj.getMeterStop());
		stopTxn.setIdTag(stopTxnObj.getIdTag());
		stopTxn.setTimeStamp(stopTxnObj.getTimeStamp());
		stopTxn.setReason(stopTxnObj.getReason());
//		stopTxn.setConnectorId(stopTxnObj.getConnectorId());
//		stopTxn.setSessionId(stopTxnObj.getSessionId());

		siv.setRequest(requestMessage);
		siv.setResponse("[3,\"" + finalData.getSecondValue()+ "\",{\"idTagInfo\":{\"status\":\"Accepted\"}}]");
		siv.setStnRefNum(stationRefNum);
		siv.setTransactionId(stopTxnObj.getTransactionId());
		siv.setReasonForTermination(stopTxnObj.getReason() == null ? "EvDisconnected" : stopTxnObj.getReason());
		siv.setEnergyActiveImportRegisterUnit("Wh");
		siv.setEnergyActiveImportRegisterValue(String.valueOf(stopTxnObj.getMeterStop()));
		siv.setStnId(stnId);
		try {
			if (stationRefNum != null && !stationRefNum.equalsIgnoreCase("") && !stationRefNum.equalsIgnoreCase("null") && siv.getTransactionId() > 0) {
				OCPPStartTransaction startTxnObj = stationService.getStartTransactionWithRfid(siv);
				if(startTxnObj != null) {
					siv.setStartTransTimeStamp(startTxnObj.getTimeStamp());
					siv.setStTxnObj(startTxnObj);
					siv.setPortId(startTxnObj.getConnectorId());
					siv.setAmpFlag(stationService.getAmpFlagByStnId(stnId));
					OCPPTransactionData txnData = ocppMeterValueService.getTxnData(startTxnObj);
					OCPITransactionData ocpitxnData = ocppMeterValueService.getOCPITxnData(startTxnObj);
					if(ocpitxnData != null && ocpitxnData.getUserType().equalsIgnoreCase("OCPI")) {
						siv.setChargeSessUniqId(ocpitxnData.getSessionId());
						ocppMeterValueService.ocpiMeterStopCall(requestMessage, stationRefNum,ocpitxnData.getSessionId(),startTxnObj.getStationId());
					}else if(txnData != null && !txnData.getUserType().equalsIgnoreCase("OCPI")){
						stopTxn.setConnectorId(objectMapper.readTree(txnData.getStn_obj()).get("connector_id").asLong());
						stopTxn.setSessionId(startTxnObj.getSessionId());
						siv.setStnObj(objectMapper.readTree(txnData.getStn_obj()));
						siv.setStnId(Long.valueOf(String.valueOf(siv.getStnObj().get("stnId").asLong())));
						siv.setSiteObj(objectMapper.readTree(txnData.getSite_obj()));
						siv.setChargeSessUniqId(txnData.getSessionId());
						siv.setTxnData(txnData);
						siv.setSesspricings(ocppMeterValueService.getSessesionPricing(txnData.getSessionId()));
						siv.setMeterValueTimeStatmp(stopTxnObj.getTimeStamp());
						siv.setUserObj(objectMapper.readTree(txnData.getUser_obj()));
						siv.setPreviousSessionData(ocppMeterValueService.getPreviousSessionData(txnData.getSessionId()));
						siv.setSessionDuration(new BigDecimal(String.valueOf(utils.getTimeDifferenceInMiliSec(startTxnObj.getTimeStamp(), siv.getMeterValueTimeStatmp()).get("Minutes"))).setScale(20, RoundingMode.HALF_DOWN));
						siv.setStop(true);
						siv = meterValueReadings.energyConsumptionCalc(siv);
						siv.setBillSessionDuration(siv.getSessionDuration());
						siv = ocppMeterValueService.billing(siv);
						siv = ocppMeterValueService.amountDeduction(siv);
						siv = ocppMeterValueService.insertIntoStopSessionData(siv);
						ocppMeterValueService.insertIntoSessionPricings(siv);
						ocppMeterValueService.deleteTxnData(siv);
						ocppMeterValueService.deleteOCPPSessionData(txnData.getSessionId());
						ocppMeterValueService.sendPushNotification(siv,"Meter Value");
						siv=alertsService.chargingActivityMail(siv);
						siv=alertsService.smsChargingSessionSummaryData(siv);
						siv=alertsService.notifyChargingSessionSummaryData(siv);
						alertsService.mailInActiveBilling(siv);
						alertsService.notifyInActiveBilling(siv);
						alertsService.alertPAYGStop(siv);
						alertsService.sendMeterValueViolationAlert(siv);
						ocppMeterValueService.sessionInterruptAlert(siv,stopTxnObj);
						ocppMeterValueService.insertStopTransaction(stopTxn);
						lmRequestService.sendingRequestsToLM("StopTransaction", requestMessage, siv, stopTxnObj.getTimestampStr().replace("T", " ").replace("Z", ""), Long.valueOf(String.valueOf(siv.getStnObj().get("connector_id").asLong())));
						chargingIntervalServiceImpl.chargingIntervalDataLogs(siv);
					}else {
						logger.info(Thread.currentThread().getId()+"Invalid stop transaction already stopped clientId/TransactionId is : "+stationRefNum+", txnId : "+siv.getTransactionId());
					}
					statusNotificationService.sendNotificationForPortStatus("", "Port Status",
							stationRefNum, Utils.getIntRandomNumber(), siv.getStnId(), siv.getPortId(),
							Long.valueOf(String.valueOf(siv.getSiteObj() == null ? 0 : siv.getSiteObj().get("siteId").asLong())),siv);
					ocppMeterValueService.insertMeterValues(siv,finalData);
					customLogger.sessionlog(siv);
				}else {
					lmRequestService.getFleetDataByTransactionId(requestMessage, siv, stopTxnObj);
				}
			}else {
				logger.info(Thread.currentThread().getId()+"Invalid stop request clientId/TransactionId is : "+stationRefNum+", txnId : "+siv.getTransactionId());
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		try {
			String response="[3,\"" + finalData.getSecondValue()+ "\",{\"idTagInfo\":{\"status\":\"Accepted\"}}]";
			esLoggerUtil.insertStationLogs(finalData.getSecondValue(),"Charger","StopTransaction",String.valueOf(requestMessage),stationRefNum,response,"Accepted",siv.getStnId(),siv.getStnObj() == null ? 0 : siv.getStnObj().get("connector_id").asLong());
		} catch (Exception e) {
			logger.error("",e);
		}
	}

}
