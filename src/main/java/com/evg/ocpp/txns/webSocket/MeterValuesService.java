package com.evg.ocpp.txns.webSocket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.evg.ocpp.txns.Service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.ServiceImpl.chargingIntervalServiceImpl;
import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.MeterValue;
import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.ocpp.OCPITransactionData;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.utils.EsLoggerUtil;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MeterValuesService {

	ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private StationService stationService;

	@Autowired
	private ocppMeterValueService ocppMeterValueService;

	@Autowired
	private LoggerUtil customLogger;

	@Autowired
	private MeterValueReadings meterValueReadings;

	@Autowired
	private EsLoggerUtil esLoggerUtil;

	@Autowired
	private Utils utils;

	@Autowired
	private StatusNotificationService statusNotificationService;

	@Autowired
	private chargingIntervalServiceImpl chargingIntervalServiceImpl;

	@Value("${revenue.Alert}")
	protected double revenueAlert;

	@Value("${PushNtfyForEnergyUse}")
	private Integer pushNtfyForEnergyUseCount;

	@Autowired
	private LMRequestService lmRequestService;

	@Autowired
	private OfflineTransactionHandlerService offlineTransactionHandler;

	private final static Logger logger = LoggerFactory.getLogger(MeterValuesService.class);

	@SuppressWarnings("unused")
	public void meterVal(FinalData finalData, String stationRefNum, String requestMessage) {
		SessionImportedValues siv = new SessionImportedValues();
		try {
			MeterValues meterValuesObj = finalData.getMeterValues();
			siv.setRequest(requestMessage);
			siv.setUnqReqId(finalData.getSecondValue());
			siv.setResponse("[3,\"" + siv.getUnqReqId() + "\",{}]");
			siv.setStnRefNum(stationRefNum);
			siv.setTransactionId(meterValuesObj.getTransactionId());
			siv.setMeterValueTimeStatmp(utils.getUtcDateFormate(new Date()));
			siv.setConnectorId(meterValuesObj.getConnectorId());
			if (stationRefNum != null && !stationRefNum.equalsIgnoreCase("") && !stationRefNum.equalsIgnoreCase("null") && siv.getTransactionId() > 0) {
				siv.setStnObj(objectMapper.readTree(String.valueOf(stationService.getStnObjByRefNos(stationRefNum,siv.getConnectorId()))));
				if (siv.getStnObj().size() > 0) {
					siv.setStnId(Long.valueOf(String.valueOf(siv.getStnObj().get("stnId").asText())));
					siv.setPortId(Long.valueOf(String.valueOf(siv.getStnObj().get("portId").asText())));
					siv.setAmpFlag(Boolean.valueOf(String.valueOf(siv.getStnObj().get("ampFlag").asText())));
					OCPPStartTransaction startTxnObj = stationService.getStartTransaction(siv);
					siv.setStTxnObj(startTxnObj);
					if(startTxnObj != null) {
						siv.setStartTransTimeStamp(startTxnObj.getTimeStamp());
						OCPPTransactionData txnData = ocppMeterValueService.getTxnData(startTxnObj);
						if(txnData != null && !txnData.getUserType().equalsIgnoreCase("OCPI")) {
							siv.setChargeSessUniqId(txnData.getSessionId());
							siv = offlineTransactionHandler.handleOfflineTransaction(siv, startTxnObj, meterValuesObj);
							for (MeterValue mValue : meterValuesObj.getMeterValues()) {
//								siv.setMeterValueTimeStatmp(mValue.getTimestamp());
								siv = meterValueReadings.MeterValueReadings(finalData,stationRefNum, siv, mValue,startTxnObj);
							}
							siv.setStnObj(objectMapper.readTree(txnData.getStn_obj()));
							siv.setSiteObj(objectMapper.readTree(txnData.getSite_obj()));
							siv.setTxnData(txnData);
							siv.setSesspricings(ocppMeterValueService.getSessesionPricing(txnData.getSessionId()));
							siv.setPreviousSessionData(ocppMeterValueService.getPreviousSessionData(txnData.getSessionId()));
							siv.setSessionDuration(new BigDecimal(String.valueOf(utils.getTimeDifferenceInMiliSec(startTxnObj.getTimeStamp(), siv.getMeterValueTimeStatmp()).get("Minutes"))).setScale(20, RoundingMode.UP));
							siv = meterValueReadings.energyConsumptionCalc(siv, startTxnObj);
							siv.setBillSessionDuration(siv.getSessionDuration());
							siv.setUserObj(objectMapper.readTree(siv.getTxnData().getUser_obj()));
							siv = ocppMeterValueService.billing(siv);
							siv = ocppMeterValueService.insertIntoSession(siv);
							ocppMeterValueService.insertIntoSessionPricings(siv);
							ocppMeterValueService.sendPushNotification(siv, "Meter Value");
							statusNotificationService.sendNotificationForPortStatus("", "Port Status",
									stationRefNum, Utils.getIntRandomNumber(), siv.getStnId(), siv.getPortId(),
									Long.valueOf(siv.getSiteObj() != null ? siv.getSiteObj().get("siteId").asLong() : 0), siv);
							ocppMeterValueService.lowBalanceValidation(siv);
							ocppMeterValueService.updateTxnData(siv.getTxnData());
							lmRequestService.sendingRequestsToLM("MeterValue", String.valueOf(requestMessage), siv, meterValuesObj.getTimestampStr().replace("T", " ").replace("Z", ""), meterValuesObj.getConnectorId());

							chargingIntervalServiceImpl.chargingIntervalDataLogs(siv);

						}else{
							OCPITransactionData ocpitxnData = ocppMeterValueService.getOCPITxnData(startTxnObj);
							if(ocpitxnData!=null && ocpitxnData.getUserType().equalsIgnoreCase("OCPI")) {
								siv.setChargeSessUniqId(ocpitxnData.getSessionId());
								ocppMeterValueService.ocpiMeterReadingCall(String.valueOf(requestMessage),stationRefNum,ocpitxnData.getSessionId());
							}else {
								logger.info(Thread.currentThread().getId()+"Invalid metervalue transaction settled : "+startTxnObj.getTransactionId());
							}
						}
						ocppMeterValueService.insertMeterValues(siv,finalData);
//						  statusNotificationService.updateOcppStatusNotification("Charging", startTxnObj.getStationId(), startTxnObj.getConnectorId(),siv.isAmpFlag(), 0, true);
						customLogger.sessionlog(siv);
					}else {
						logger.info(Thread.currentThread().getId()+"Invalid metervalue request start transaction is not exist");
						try {
							lmRequestService.sendInvalidMeterValuesToLM(String.valueOf(requestMessage), siv, meterValuesObj, finalData,stationRefNum, startTxnObj);
						} catch (Exception e) {
							logger.error("",e);
						}
					}
				}else {
					logger.info(Thread.currentThread().getId()+"Invalid metervalue request stationId/portId is not exist is : "+stationRefNum+", connectorId : "+siv.getConnectorId());
				}
			}else {
				logger.info(Thread.currentThread().getId()+"Invalid metervalue request clientId/TransactionId is : "+stationRefNum+", txnId : "+siv.getTransactionId());
			}
		} catch (Exception e) {
			logger.error("",e);
		}
		try {
			String response = "[3,\"" + finalData.getSecondValue() + "\",{}]";
			esLoggerUtil.insertStationLogs(finalData.getSecondValue(),"Charger","MeterValues",String.valueOf(requestMessage),stationRefNum,response,"Accepted",siv.getStnId(),siv.getConnectorId());
		}catch (Exception e) {
			logger.error("",e);
		}
	}
}
