package com.evg.ocpp.txns.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.MeterValue;
import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.SampledValue;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.forms.TransactionData;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Service
public class JSONDataParser {

	Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Autowired
	private LoggerUtil customLogger;
	
	@Autowired
	private Utils utils;
	
	private final static Logger logger = LoggerFactory.getLogger(JSONDataParser.class);
	
	@SuppressWarnings("rawtypes")
	public FinalData getData(String jsonData,String clientId) {
		// TODO Auto-generated method stub
		FinalData finalData = new FinalData();
		try {
		boolean jsonValid = isJSONValid(jsonData);
		if (jsonValid) {
			try {
				JSONParser jsonParser = new JSONParser();
				Object mainObj = jsonParser.parse(jsonData);
				JSONArray jsonArry = (JSONArray) mainObj;
				List<SampledValue> sampledValues = new ArrayList<SampledValue>();
				List<MeterValue> metervalueData = new ArrayList<MeterValue>();
				List<TransactionData> transactionData = new ArrayList<TransactionData>();
				finalData.setFirstValue(9l);
				for (Object object1 : jsonArry) {
					finalData.setFirstValue((Long) jsonArry.get(0));
					finalData.setSecondValue((String) jsonArry.get(1));
					if (object1 != null) {
						if (object1.equals("MeterValues")) {
							MeterValues meterValues = new MeterValues();
							for (Object o : jsonArry) {
								if (o instanceof JSONObject) {
									Iterator iter = ((JSONObject) o).entrySet().iterator();
									while (iter.hasNext()) {
										Map.Entry me = (Map.Entry) iter.next();
										if (me.getValue() instanceof JSONArray) {
											for (Object object : (JSONArray) me.getValue()) {
												MeterValue meterValue = new MeterValue();
												if (object instanceof JSONObject) {
													Iterator iter1 = ((JSONObject) object).entrySet().iterator();
													while (iter1.hasNext()) {
														Map.Entry me1 = (Map.Entry) iter1.next();
														if (me1.getValue() instanceof JSONArray) {
															for (Object obj : (JSONArray) me1.getValue()) {
																if (obj instanceof JSONObject) {
																	SampledValue sampledValue = new SampledValue();
																	Iterator<?> iter2 = ((JSONObject) obj).entrySet()
																			.iterator();
																	while (iter2.hasNext()) {
																		Map.Entry me2 = (Map.Entry) iter2.next();
																		if (me2.getKey().equals("phase"))
																			sampledValue.setPhase((String) me2.getValue());
																		if (me2.getKey().equals("unit"))
																			sampledValue.setUnit((String) me2.getValue());
																		if (me2.getKey().equals("context"))
																			sampledValue
																					.setContext((String) me2.getValue());
																		if (me2.getKey().equals("format"))
																			sampledValue.setFormat((String) me2.getValue());
																		if (me2.getKey().equals("measurand"))
																			sampledValue
																					.setMeasurand((String) me2.getValue());
																		if (me2.getKey().equals("location"))
																			sampledValue
																					.setLocation((String) me2.getValue());
																		if (me2.getKey().equals("value"))
																			sampledValue.setValue(
																					String.valueOf(me2.getValue()));
																	}
																	sampledValues.add(sampledValue);
																}
															}
														} else {
															if (me1.getKey().equals("timestamp")) {
																if (me1.getValue() == null || me1.getValue() == "" || me1.getValue().equals("")) {
																	meterValue.setTimestamp(Utils.getUTCDate());
																	meterValues.setTimestampStr(Utils.getUTCDateString());
																} else {
																	Date startTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(String.valueOf(me1.getValue()).replace("T", " ").replace("Z", ""));
																	meterValue.setTimestamp(startTimeStamp);
																	meterValues.setTimestampStr(utils.stringToDate(startTimeStamp));
																}
															}

														}
													}
													meterValue.setSampledValue(sampledValues);
													metervalueData.add(meterValue);
												}
											}
										} else {
											if (me.getKey().equals("connectorId")) {
												if (me.getValue() == null || me.getValue() == "") {
													meterValues.setConnectorId(0);
												} else {
													long key = ((long) me.getValue());
													meterValues.setConnectorId((int) key);
												}

											}
											if (me.getKey().equals("transactionId")) {
												if (me.getValue() == null || me.getValue() == "") {
													meterValues.setTransactionId(0);
												} else {
													long key = ((long) me.getValue());
													meterValues.setTransactionId((int) key);
												}

											}
										}
									}
									meterValues.setMeterValues(metervalueData);
								}
							}
							finalData.setMeterValues(meterValues);
						} 
						 else if (object1.equals("StopTransaction")) {
								StopTransaction stopTransaction = new StopTransaction();
								for (Object o : jsonArry) {
									if (o instanceof JSONObject) {
										Iterator<?> iter = ((JSONObject) o).entrySet().iterator();
										String idTag = "";
										while (iter.hasNext()) {
											try {

												Map.Entry me = (Map.Entry) iter.next();
												if (me.getKey().equals("transactionId"))
													stopTransaction
															.setTransactionId((Long.parseLong(String.valueOf(me.getValue()))));
												if (me.getKey().equals("reason"))
													stopTransaction.setReason((String) me.getValue());
												if (me.getKey().equals("idTag")) {
													idTag = (String) me.getValue();
													stopTransaction.setIdTag(idTag);
												}
												if (me.getKey().equals("timestamp")) {
													if (me.getValue() == null || me.getValue() == "" || me.getValue().equals("")) {
														stopTransaction.setTimeStamp(Utils.getUTCDate());
														stopTransaction.setTimestampStr(Utils.getUTCDateString());
													} else {
														Date startTimeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(String.valueOf(me.getValue()).replace("T", " ").replace("Z", ""));
														stopTransaction.setTimeStamp(startTimeStamp);
														stopTransaction.setTimestampStr(utils.stringToDate(startTimeStamp));
													}
												}
												if (me.getKey().equals("meterStop"))
													stopTransaction.setMeterStop(Double.parseDouble(String.valueOf(me.getValue())));
												
												
												
												
												if (me.getKey().equals("transactionData")) {
//													stopTransaction.setTransactionData((List) me.getValue());
												
												if (me.getValue() instanceof JSONArray) {
													for (Object object : (JSONArray) me.getValue()) {
														TransactionData transaction = new TransactionData();
														if (object instanceof JSONObject) {
															Iterator iter1 = ((JSONObject) object).entrySet().iterator();
															while (iter1.hasNext()) {
																Map.Entry me1 = (Map.Entry) iter1.next();
																if (me1.getValue() instanceof JSONArray) {
																	for (Object obj : (JSONArray) me1.getValue()) {
																		if (obj instanceof JSONObject) {
																			SampledValue sampledValue = new SampledValue();
																			Iterator<?> iter2 = ((JSONObject) obj).entrySet()
																					.iterator();
																			while (iter2.hasNext()) {
																				Map.Entry me2 = (Map.Entry) iter2.next();
																				if (me2.getKey().equals("phase"))
																					sampledValue.setPhase((String) me2.getValue());
																				if (me2.getKey().equals("unit"))
																					sampledValue.setUnit((String) me2.getValue());
																				if (me2.getKey().equals("context"))
																					sampledValue
																							.setContext((String) me2.getValue());
																				if (me2.getKey().equals("format"))
																					sampledValue.setFormat((String) me2.getValue());
																				if (me2.getKey().equals("measurand"))
																					sampledValue
																							.setMeasurand((String) me2.getValue());
																				if (me2.getKey().equals("location"))
																					sampledValue
																							.setLocation((String) me2.getValue());
																				if (me2.getKey().equals("value"))
																					sampledValue.setValue(
																							String.valueOf(me2.getValue()));
																			}
																			sampledValues.add(sampledValue);
																		}
																	}
																} else {
																	if (me1.getKey().equals("timestamp")) {
																		if (me1.getValue() == null || me1.getValue() == ""
																				|| me1.getValue().equals("")) {
																			transaction.setTimestamp(Utils.getUTCDate());
																		} else {
																			Date startTimeStamp = new SimpleDateFormat(
																					"yyyy-MM-dd HH:mm:ss")
																					.parse(String.valueOf(me1.getValue())
																							.replace("T", " ").replace("Z", ""));
																			transaction.setTimestamp(startTimeStamp);
																		}
																	}

																}
															}
															transaction.setSampledValue(sampledValues);
															transactionData.add(transaction);
														}
													}
												}
												stopTransaction.setTransactionData(transactionData);
												}

												if (idTag.contains("ccIdTag")) {
													stopTransaction.setIdTag("ccIdTag");
													JSONObject authData = (JSONObject) jsonParser.parse(idTag);
													if (authData instanceof JSONObject) {
														Iterator au = ((JSONObject) authData).entrySet().iterator();
														while (au.hasNext()) {
															Map.Entry aut = (Map.Entry) au.next();
															String ccid = "";
															if (aut.getKey().equals("ccIdTag")) {
																ccid = String.valueOf(aut.getValue());
															}
															JSONObject ccidTag = (JSONObject) jsonParser.parse(ccid);
															if (ccidTag instanceof JSONObject) {
																Iterator cc = ((JSONObject) ccidTag).entrySet().iterator();
																while (cc.hasNext()) {
																	Map.Entry ccInfo = (Map.Entry) cc.next();
																	if (ccInfo.getKey().equals("CCDATA")) {
																		stopTransaction.setCcdata(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("maskedData")) {
																		stopTransaction.setMaskeddata(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("cardType")) {
																		stopTransaction.setCardType(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("decryptedDataLen")) {
																		stopTransaction.setDecrypteddataLen(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("KSN")) {
																		stopTransaction.setKsn(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("type")) {
																		stopTransaction.setType(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("drCode")) {
																		stopTransaction.setDrCode(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("priceCode")) {
																		stopTransaction.setPricecode(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("vendorId")) {
																		stopTransaction.setVendorid(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																	if (ccInfo.getKey().equals("paymentcode")) {
																		stopTransaction.setPaymentcode(gson.toJson(String
																				.valueOf(ccInfo.getValue()).replace("\"", "")));
																	}
																	if (ccInfo.getKey().equals("phone")) {
																		stopTransaction.setPhone(gson.toJson(String
																				.valueOf(ccInfo.getValue()).replace("\"", "")));
																	}
																	if (ccInfo.getKey().equals("paymentmode")) {
																		stopTransaction.setPaymentmode(
																				gson.toJson(String.valueOf(ccInfo.getValue())));
																	}
																}
															}
														}
													}
												}

											} catch (Exception e) {
												logger.error("",e);
											}
										}
									}
								}
								finalData.setStopTransaction(stopTransaction);
							}
					}	
				}
			}catch (Exception e) {
				customLogger.info("Error", "invalid request/response from "+clientId+": "+jsonData+" / final data obj : "+finalData);
				logger.error("",e);
			}
		}else {
			finalData.setFirstValue(0l);
			finalData.setStnReferNum(clientId);
			customLogger.info("Error", "invalid json format request from "+clientId+" : "+jsonData+" / final data obj : "+finalData);
		}
		return finalData;
	}catch(Exception e) {
		logger.error("",e);
		customLogger.info("Error", "getData method at stationId : "+clientId+" / exception name : "+e+" / Received Message : "+jsonData+" / final data obj : "+finalData);
	}
	return finalData;
}
	public static boolean isJSONValid(String jsonInString ) {
	    try {
	       if(jsonInString != null && !jsonInString.equalsIgnoreCase("null") && !jsonInString.equalsIgnoreCase("")) {
	    	   final ObjectMapper mapper = new ObjectMapper();
		       mapper.readTree(jsonInString);
		       return true;
	       }else {
	    	   return false;
	       }
	    } catch (Exception e) {
	       return false;
	    }
	  }
	public long getUniqConnectorId(String stnReferNo ,String connectorId) {
		long val = 0;
		try {
			customLogger.info(stnReferNo,"inside method : "+stnReferNo+" , connectorId "+connectorId);
			String uniqPortIdQuery="select p.id from port p inner join station s on p.station_id = s.id "
					+ " where s.referNo='"+stnReferNo+"' and p.connector_id='"+connectorId+"'";
			customLogger.info(stnReferNo,"uniqPortIdQuery : "+uniqPortIdQuery);
			List<Map<String, Object>> findAll = executeRepository.findAll(uniqPortIdQuery);
			if(findAll.size() > 0) {
				val = Long.valueOf(String.valueOf(findAll.get(0).get("id")));
			}else {
				val = 0;
			}
			customLogger.info(stnReferNo,"uniqPortId : "+val);
		}catch(Exception e) {
			logger.error("",e);
		}
		return val;
	}
}