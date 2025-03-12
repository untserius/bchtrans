package com.evg.ocpp.txns.utils;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.forms.OCPPMeterValuesPojo;
import com.evg.ocpp.txns.model.es.OCPPChargingIntervalData;
import com.evg.ocpp.txns.model.es.OcppMeterValues;
import com.evg.ocpp.txns.model.es.StationLogs;
import com.evg.ocpp.txns.repository.es.EVGRepositoryChargingInterval;

@Service
public class EsLoggerUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(EsLoggerUtil.class);
	
	@Value("${es.stationlogs}")
	private String OCPPLOGS_INDEX;
	
	@Value("${es.metervaluelogs}")
	private String OCPPMERTERVALUELOGS_INDEX;
	
	@Value("${es.portstatuslogs}")
	private String CREATEPORTERRORSTATUS_INDEX;
	
	@Value("${es.ocppchargingintervallogs}")
	private String OCPPCHARGINGINTERVALDATALOGS_INDEX;
	
	@Autowired
	private EVGRepositoryChargingInterval chargingInterval;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;
	

	@Autowired
	private Utils utils;
	
	public void createOCPPChargingIntervalDataIndex(OCPPChargingIntervalData ocppChargingIntervalData) {
		try {
			ocppChargingIntervalData.setCreatedTimestamp(new Date());
			IndexQuery indexQuery = new IndexQueryBuilder().withId(String.valueOf(ocppChargingIntervalData.getId())).withObject(ocppChargingIntervalData).build();
			elasticsearchOperations.index(indexQuery, IndexCoordinates.of(OCPPCHARGINGINTERVALDATALOGS_INDEX));
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public OCPPChargingIntervalData getChargingIntervalData(String sessionId) {
		OCPPChargingIntervalData ocppChargingIntervalData = null;
		try {
			if(chargingInterval.existsById(sessionId)) {
				Optional<com.evg.ocpp.txns.model.es.OCPPChargingIntervalData> findById = chargingInterval.findById(sessionId);
				if (findById != null && findById.isPresent() && findById.get()!=null) {
					ocppChargingIntervalData=findById.get();
				}
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return ocppChargingIntervalData;
	}
	public void createOcppMeterLogsIndex(OCPPMeterValuesPojo meterValues,String clientId,String reqId,Date createdTimestamp) {
		Thread th = new Thread() {
			public void run() {
				String id =reqId;
				try {
					if(clientId.equalsIgnoreCase("Portal") || clientId.equalsIgnoreCase("Android") || clientId.equalsIgnoreCase("Ios") || clientId.equalsIgnoreCase("ocpi")) {
						String[] sedVal = reqId.split(":");
						if (sedVal.length > 1) {
							id = sedVal[0];
						}
					}
				}catch (Exception e) {
					logger.error("",e);
				}
				try {						
					OcppMeterValues log = new OcppMeterValues();
					log.setId(utils.getuuidRandomId(meterValues.getStationId()));
					log.setSessionId(meterValues.getSessionId());
					log.setTransactionId(String.valueOf(meterValues.getTransactionId()));
					log.setPortId(meterValues.getPortId());
					log.setMeterValueTimeStamp(meterValues.getTimeStamp());
					log.setCreatedTimestamp(createdTimestamp);

					log.setEnergyActiveExportRegisterUnit(meterValues.getEnergyActiveExportRegisterUnit());
					log.setEnergyActiveImportRegisterUnit(meterValues.getEnergyActiveImportRegisterUnit());
					log.setEnergyReactiveImportRegisterUnit(meterValues.getEnergyReactiveImportRegisterUnit());
					log.setEnergyReactiveExportRegisterUnit(meterValues.getEnergyReactiveExportRegisterUnit());
					log.setEnergyActiveExportIntervalUnit(meterValues.getEnergyActiveExportIntervalUnit());
					log.setEnergyActiveImportIntervalUnit(meterValues.getEnergyActiveImportIntervalUnit());
					log.setEnergyReactiveExportIntervalUnit(meterValues.getEnergyReactiveExportIntervalUnit());
					log.setEnergyReactiveImportIntervalUnit(meterValues.getEnergyReactiveImportIntervalUnit());
					log.setPowerActiveExportUnit(meterValues.getPowerActiveExportUnit());
					log.setPowerActiveImportUnit(meterValues.getPowerActiveImportUnit());
					log.setPowerOfferedUnit(meterValues.getPowerOfferedUnit());
					log.setPowerReactiveExportUnit(meterValues.getPowerReactiveExportUnit());
					log.setPowerReactiveImportUnit(meterValues.getPowerReactiveImportUnit());
					log.setPowerFactorUnit(meterValues.getPowerFactorUnit());
					log.setCurrentImportUnit(meterValues.getCurrentImportUnit());
					log.setCurrentExportUnit(meterValues.getCurrentExportUnit());
					log.setCurrentOfferedUnit(meterValues.getCurrentOfferedUnit());
					log.setVoltageUnit(meterValues.getVoltageUnit());
					log.setFrequencyUnit(meterValues.getFrequencyUnit());
					log.setTemperatureUnit(meterValues.getTemperatureUnit());
					log.setSoCUnit(meterValues.getSoCUnit());
					log.setRPMUnit(meterValues.getRPMUnit());
					
					log.setEnergyActiveExportRegisterValue(meterValues.getEnergyActiveExportRegisterValue());
					log.setEnergyActiveImportRegisterValue(meterValues.getEnergyActiveImportRegisterValue());
					log.setEnergyActiveImportRegisterDiffValue(meterValues.getEnergyActiveImportRegisterDiffValue());
					log.setEnergyReactiveExportRegisterValue(meterValues.getEnergyReactiveExportRegisterValue());
					log.setEnergyReactiveImportRegisterValue(meterValues.getEnergyReactiveImportRegisterValue());
					log.setEnergyActiveExportIntervalValue(meterValues.getEnergyActiveExportIntervalValue());
					log.setEnergyActiveImportIntervalValue(meterValues.getEnergyActiveImportIntervalValue());
					log.setEnergyReactiveExportIntervalValue(meterValues.getEnergyReactiveExportIntervalValue());
					log.setEnergyReactiveImportIntervalValue(meterValues.getEnergyReactiveImportIntervalValue());
					log.setPowerActiveExportValue(meterValues.getPowerActiveExportValue());
					log.setPowerActiveImportValue(meterValues.getPowerActiveImportValue());
					log.setPowerOfferedValue(meterValues.getPowerOfferedValue());
					log.setPowerReactiveExportValue(meterValues.getPowerReactiveExportValue());
					log.setPowerReactiveImportValue(meterValues.getPowerReactiveImportValue());
					log.setPowerFactorValue(meterValues.getPowerFactorValue());
					log.setCurrentImportValue(meterValues.getCurrentImportValue());
					log.setCurrentExportValue(meterValues.getCurrentExportValue());
					log.setCurrentOfferedValue(meterValues.getCurrentOfferedValue());
					log.setVoltageValue(meterValues.getVoltageValue());
					log.setFrequencyValue(meterValues.getFrequencyValue());
					log.setTemperatureValue(meterValues.getTemperatureValue());
					log.setSoCValue(meterValues.getSoCValue());
					log.setRPMValue(meterValues.getRPMValue());
					log.setPowerActiveImportDiffValue(meterValues.getPowerActiveImportDiffValue());
					log.setCurrentImportDiffValue(meterValues.getCurrentImportDiffValue());
					log.setSoCDiffValue(meterValues.getSoCDiffValue());
					createOcppMeterLogsIndex(log);
				} catch (Exception e) {
					logger.error("",e);
				}
			}
		};
		th.start();	
	}
	public void createOcppMeterLogsIndex(OcppMeterValues meterValues) {
		try {
			meterValues.setCreatedTimestamp(new Date());
			IndexQuery indexQuery = new IndexQueryBuilder().withId(String.valueOf(meterValues.getId())).withObject(meterValues).build();
			elasticsearchOperations.index(indexQuery, IndexCoordinates.of(OCPPMERTERVALUELOGS_INDEX));
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public void insertLongs(String reqId, String clientId, String reqType, String request, String referNo,long stationId) {
		try {
			Thread th = new Thread() {
				public void run() {
					String id =reqId;
					if(clientId.equalsIgnoreCase("Portal") || clientId.equalsIgnoreCase("Android") || clientId.equalsIgnoreCase("Ios") || clientId.equalsIgnoreCase("ocpi")) {
						String[] sedVal = reqId.split(":");
						if (sedVal.length > 1) {
							id = sedVal[0];
						}
					}
			
					StationLogs log = new StationLogs();
					log.setId(id);
					log.setStationId(stationId);
					log.setRequest(request);
					log.setRequestType(reqType);
					log.setStatus("Inprogress");
					log.setRequestId(reqId);
					log.setStnRefNum(referNo);
					log.setClientId(clientId);
					esInfo(log);
				}
			};
			th.start();
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public void esInfo(StationLogs log) {
		try {
			Thread th = new Thread() {
				public void run() {
					createOcppLogsIndex(log);
				}
			};
			th.start();
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public void createOcppLogsIndex(StationLogs logs) {
		try {
			Thread th = new Thread() {
				public void run() {
					logs.setCreatedTimestamp(new Date());
					IndexQuery indexQuery = new IndexQueryBuilder().withId(String.valueOf(logs.getId())).withObject(logs).build();
					elasticsearchOperations.index(indexQuery, IndexCoordinates.of(OCPPLOGS_INDEX));
				}
			};
			th.start();
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	public void insertStationLogs(String reqId, String clientId, String reqType, String request, String referNo,String response,String status,long stationId,long connectorId) {
		try {
			Thread th = new Thread() {
				public void run() {
					String id =reqId;
					try {
						if(clientId.equalsIgnoreCase("Portal") || clientId.equalsIgnoreCase("Android") || clientId.equalsIgnoreCase("Ios") || clientId.equalsIgnoreCase("ocpi")) {
							String[] sedVal = reqId.split(":");
							if (sedVal.length > 1) {
								id = sedVal[0];
							}
						}
					}catch (Exception e) {
						logger.error("",e);
					}
					try {
						StationLogs log = new StationLogs();
						log.setId(utils.getRandomNumber(""));
						log.setStationId(stationId);
						log.setRequest(request);
						log.setRequestType(reqType);
						log.setStatus(status);
						log.setResponse(response);
						log.setRequestId(reqId);
						log.setStnRefNum(referNo);
						log.setClientId(clientId);
						log.setConnectorId(connectorId);
						esInfo(log);
					} catch (Exception e) {
						logger.error("",e);
					}
				}
			};
			th.start();
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
}
