package com.evg.ocpp.txns.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.StationService;
import com.evg.ocpp.txns.Service.alertsService;
import com.evg.ocpp.txns.Service.configService;
import com.evg.ocpp.txns.Service.ocppUserService;
import com.evg.ocpp.txns.Service.sessionDataService;
import com.evg.ocpp.txns.config.EmailServiceImpl;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.IdleCharging;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.taxes;
import com.evg.ocpp.txns.model.ocpp.DeviceDetails;
import com.evg.ocpp.txns.model.ocpp.OCPPTransactionData;
import com.evg.ocpp.txns.model.ocpp.PreferredNotification;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.PushNotification;
import com.evg.ocpp.txns.utils.Utils;
import com.evg.ocpp.txns.utils.smsIntegration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class alertsServiceImpl implements alertsService{
	private final static Logger logger = LoggerFactory.getLogger(alertsServiceImpl.class);
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Autowired
	private smsIntegration smsIntegrationImpl;
	
	@Autowired
	private EmailServiceImpl emailServiceImpl;
	
	@Autowired
	private sessionDataService sessionDataService;
	
	@Autowired
	private configService configService;
	
	@Autowired
	private StationService stationService;
	
	@Autowired
	private Utils utils;
	
	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Autowired
	private ocppUserService ocppUserService;
	
	@Autowired
	private PushNotification pushNotification;
	
	@Autowired
	private ExecuteRepository executeRepository;
	
	@Override
	public PreferredNotification preferredNotify(long userId) {
		PreferredNotification pn = null;
		try {
			pn = generalDao.findOne("FROM PreferredNotification where userId="+userId, new PreferredNotification());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return pn;
	}
	
	@Override
	public String mailChargingSessionSummaryData(String sessionId,boolean idle) {
		try {
			Map<String, Object> sessionData = sessionDataService.getSessionData(sessionId);
			Map<String, Object> userObj=new HashMap<>();
			if(sessionData != null && !sessionData.isEmpty()) {
				PreferredNotification pn = preferredNotify(Long.valueOf(String.valueOf(sessionData.get("userId"))));
				if((pn != null && pn.isEmailChargingCompleted())) {
					userObj = ocppUserService.getUserDataByUserId(Long.parseLong(String.valueOf(sessionData.get("userId"))));
					String rfidByRfidHex = ocppUserService.getRfidByRfidHex(String.valueOf(sessionData.get("customerId")));
					if(rfidByRfidHex != null) {
						userObj.put("digitalId", rfidByRfidHex);
					}
					boolean flag=mail(sessionData,userObj,0);
					if(flag) {
						return "success";
					}else {
						return "failed";
					}
				}else if(String.valueOf(sessionData.get("txnType")).equalsIgnoreCase("PAYG")) {
					userObj = ocppUserService.getUserDataBySessionIdPayg(sessionId);
					mail(sessionData,userObj,0);
					return "success";
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}
		return "failed";
	}
	
	@Override
	public String notification(String sessionId,String notifyType) {
		try {
			Thread th = new Thread() {
				public void run() {
					Map<String, Object> sessionData = sessionDataService.getSessionData(sessionId);
					if(sessionData != null && !sessionData.isEmpty()) {
						PreferredNotification pn = preferredNotify(Long.valueOf(String.valueOf(sessionData.get("userId"))));
						Map<String, Object> userObj=new HashMap<>();
						if(pn!=null && notifyType.equalsIgnoreCase("mail") && pn.isEmailChargingCompleted()) {
							userObj = ocppUserService.getUserDataByUserId(Long.parseLong(String.valueOf(sessionData.get("userId"))));
							String rfidByRfidHex = ocppUserService.getRfidByRfidHex(String.valueOf(sessionData.get("customerId")));
							if(rfidByRfidHex != null) {
								userObj.put("digitalId", rfidByRfidHex);
							}
							boolean flag=mail(sessionData,userObj,1);
						}else if(pn!=null && notifyType.equalsIgnoreCase("sms") && pn.isSmsChargingCompleted()) {
							userObj = ocppUserService.getUserDataByUserId(Long.parseLong(String.valueOf(sessionData.get("userId"))));
							String phoneNumber = String.valueOf(userObj.get("phoneNumber"));
							boolean flag=smsIntegrationImpl.sendSMSUsingBootConfg(String.valueOf(sessionData.get("stnRefNum")),String.valueOf(sessionData.get("sessId")),"NrmlUserStop",Double.parseDouble(String.valueOf(sessionData.get("finalAmount"))),phoneNumber,null,null,sessionId,1);
						}else if(pn!=null && notifyType.equalsIgnoreCase("pushNotification") && pn.isNotificationChargingCompleted()) {
							userObj = ocppUserService.getUserDataByUserId(Long.parseLong(String.valueOf(sessionData.get("userId"))));
							boolean flag=chargingSummaryNotification(sessionData,userObj,sessionId);
						}
					}
				}
			};
			th.start();
		}catch (Exception e) {
			e.printStackTrace();
			return "failed";
		}
		return "success";
	}
	
	@Override
	public boolean chargingSummaryNotification(Map<String,Object> sessionObj,Map<String,Object> userObj,String sessionId) {
		try {
			JSONObject map = new JSONObject();
			map.put("notificationId", utils.getRandomNumber(""));
			map.put("type", "Charging Summary");
			map.put("categoryIdentifier", "notification");
			map.put("energyDelivered", String.valueOf(new BigDecimal(String.valueOf(sessionObj.get("totalkWh"))).setScale(4,RoundingMode.DOWN)));
			map.put("finalCost", String.valueOf(new BigDecimal(String.valueOf(sessionObj.get("finalAmount"))).setScale(2,RoundingMode.DOWN))); 
			map.put("referNo", String.valueOf(sessionObj.get("stnRefNum"))); 
			map.put("stationName", String.valueOf(sessionObj.get("stationName")));
			map.put("duration", String.valueOf(sessionObj.get("sessionDuration")));
			map.put("connectorId", String.valueOf(sessionObj.get("connector_id")));
			map.put("sessionId", sessionId);
			map.put("currencyCode", String.valueOf(userObj.get("crncy_Code")));
			map.put("currencySymbol", String.valueOf(userObj.get("crncy_HexCode")));
			map.put("uuid", String.valueOf(userObj.get("uuid")));
			boolean flag=sendNotifications(Long.parseLong(String.valueOf(sessionObj.get("userId"))),"","Charging Session",String.valueOf(sessionObj.get("stnRefNum")),Long.parseLong(String.valueOf(userObj.get("orgId"))),sessionId,map,sessionId,1);
		    return flag;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	@Override
	public void mailInActiveBilling(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				if (siv.isIdleBilling()) {
					if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
						PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
						if (pn != null && pn.isEmailIdleBilling()) {
							inActivityMail(siv);
						}
					} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
						inActivityMail(siv);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean mail(Map<String, Object> sessionData,Map<String, Object> userObj,long resend) {
		try {
			Map<String, Object> stnObj = stationService.getStnObjByRefNosMap(String.valueOf(sessionData.get("stnRefNum")),Long.valueOf(String.valueOf(sessionData.get("connector_id"))));
			Map<String, Object> siteObj = stationService.getSiteDetails(Long.valueOf(String.valueOf(sessionData.get("stnId"))));
			Map<String, Object> configs = configService.getAlertConfigs();
			String cost_Info=String.valueOf(sessionData.get("cost_Info"));
			String rewardType=String.valueOf(sessionData.get("rewardType"));
			BigDecimal rewardValue=new BigDecimal(String.valueOf(sessionData.get("rewardValue"))).setScale(2, RoundingMode.HALF_UP);
			String currencySymbol="&#36;";
			long userTimeZoneId = getTimeZone(String.valueOf(siteObj.get("timeZone")));
			if(userObj != null && !userObj.isEmpty() && Long.parseLong(String.valueOf(sessionData.get("userId"))) != 0) {
				currencySymbol=String.valueOf(userObj.get("crncy_HexCode"));
//				userTimeZoneId = Long.valueOf(String.valueOf(userObj.get("userTimeZoneId")));
			}
			String chargingPricing="0.0";
			String rateRiderAmount="0.0";
			String rateRiderType="+";
			String rateRiderPer="0";
			String tax_name_per="TAX 0.0";
			String taxAmount="0.0";
			BigDecimal total_taxAmount=new BigDecimal("0.00");
			JsonNode tariff = objectMapper.readTree(cost_Info);
			List<taxes> taxes = new ArrayList<>();
			List<IdleCharging> idleCharging = new ArrayList<>();
			taxes flatFee = null;
			BigDecimal chargingCost=new BigDecimal("0.00");
			if(tariff.size() > 0) {
				JsonNode prices = objectMapper.readTree(String.valueOf(tariff.get(0).get("cost_info")));
				 if(prices.size() > 0) {
					 JsonNode standard = objectMapper.readTree(String.valueOf(prices.get(0).get("standard")));
					 if(standard.size() > 0) {
						 JsonNode time = objectMapper.readTree(String.valueOf(standard.get("time")));
						 if(time.size() > 0) {
							 String price =String.valueOf(time.get("price").asDouble());
							 chargingCost=new BigDecimal(time.get("tax_excl").asText());
							 double step = time.get("step").asDouble();
							 String units="Min";
							 if(step==3600) {
								 units="Hr";
							 }
							 chargingPricing=currencySymbol+price+"/"+units;
						 }
						 JsonNode energy = objectMapper.readTree(String.valueOf(standard.get("energy")));
						 if(energy.size()>0) {
							 String price =String.valueOf(energy.get("price").asDouble());
							 chargingCost=chargingCost.add(new BigDecimal(energy.get("tax_excl").asText()));
							 if(chargingPricing.equalsIgnoreCase("0.0")) {
								 chargingPricing= currencySymbol+price+"/"+"kWh";
							 }else {
								 chargingPricing=chargingPricing+" & "+currencySymbol+price+"/"+"kWh";
							 }
						 }
						 JsonNode flat = objectMapper.readTree(String.valueOf(standard.get("flat")));
						 if(flat.size()>0) {
							 flatFee=new taxes();
							 String price =currencySymbol+String.valueOf(new BigDecimal(flat.get("price").asText()).setScale(2, RoundingMode.HALF_UP));
							 flatFee.setTax_name_per("Transaction fee cost:");
							 flatFee.setTaxAmount(price);
							 
						 }
					 }
					 chargingPricing=chargingPricing+" + tax";
					 JsonNode aditional = objectMapper.readTree(String.valueOf(prices.get(0).get("aditional")));
					 if(aditional.size()>0) {
						 JsonNode rateRider = objectMapper.readTree(String.valueOf(aditional.get("rateRider")));
//						 if(rateRider.size()>0) {
//							 rateRiderAmount=rateRider.get("amount").asText();
//							 rateRiderType = rateRider.get("type").asText();
//							 rateRiderType=rateRiderType.replace("ve", "").replace("Ve", "");
//							 rateRiderPer=String.valueOf(rateRider.get("percnt").asDouble());
//							 tax_name_per = "Rate rider"+"("+rateRiderType+rateRiderPer+"%)";
//							 taxAmount=currencySymbol+""+utils.decimalwithtwodecimals(new BigDecimal(rateRiderAmount));
//							 taxes tax = new taxes();
//							 tax.setTax_name_per(tax_name_per);
//							 tax.setTaxAmount(taxAmount);
//							 taxes.add(tax);
//						 }
						 JsonNode idle = objectMapper.readTree(String.valueOf(aditional.get("idle")));
						 if(idle.size()>0) {
//							 chargingPricing=chargingPricing+" + idle";
							 tax_name_per="Idle Charge";
							 String stepSize=idle.get("step").asDouble()==60 ? "Min" :"Hr";
							 String idleFee=currencySymbol+""+String.valueOf(new BigDecimal(idle.get("price").asText()).setScale(2, RoundingMode.HALF_UP))+"/"+stepSize;
							 IdleCharging idlebill1=new IdleCharging();
							 idlebill1.setName("Idle fee price");
							 idlebill1.setValue(idleFee);
							 idleCharging.add(idlebill1);
							 String graceTime=Utils.formatDuration(idle.get("gracePeriod").asDouble());
							 IdleCharging idlebill2=new IdleCharging();
							 idlebill2.setName("Grace period duration");
							 idlebill2.setValue(graceTime);
							 idleCharging.add(idlebill2);
//							 String idleDuration=Utils.formatDuration(idle.get("idleDuration").asDouble());
//							 IdleCharging idlebill3=new IdleCharging();
//							 idlebill3.setName("Idle duration");
//							 idlebill3.setValue(idleDuration);
//							 idleCharging.add(idlebill3);
//							 String idleCost=currencySymbol+""+utils.decimalwithtwodecimals(new BigDecimal(idle.get("idleCost").asText()));
//							 IdleCharging idlebill4=new IdleCharging();
//							 idlebill4.setName("Idle cost");
//							 idlebill4.setValue(idleCost);
//							 idleCharging.add(idlebill4);
							 if(!String.valueOf(idle.get("inActiveduration")).equalsIgnoreCase("null")) {
								 String inActiveduration=Utils.formatDuration(idle.get("inActiveduration").asDouble());
								 IdleCharging idlebill5=new IdleCharging();
								 idlebill5.setName("Idle fee duration");
								 idlebill5.setValue(inActiveduration);
								 idleCharging.add(idlebill5);
								 String inActiveCost=currencySymbol+""+utils.decimalwithtwoZeros(utils.decimalwithtwodecimals(new BigDecimal(idle.get("inActiveCost").asText())));
//								 IdleCharging idlebill6=new IdleCharging();
//								 idlebill6.setName("Idle cost");
//								 idlebill6.setValue(inActiveCost);
//								 idleCharging.add(idlebill6);
								 taxes tax = new taxes();
								 tax.setTax_name_per("Idle fee cost");
								 tax.setTaxAmount(inActiveCost);
								 taxes.add(tax);
							 }
						 }
					 }
					 if(flatFee!=null) {
						 taxes.add(flatFee);
					 }
					 JsonNode taxJsonLs = objectMapper.readTree(String.valueOf(aditional.get("tax")));
					 if(taxJsonLs.size()>0) {
						 for(int i=0;i < taxJsonLs.size();i++) {
							 JsonNode taxJsonMap = objectMapper.readTree(String.valueOf(taxJsonLs.get(i)));
							 if(taxJsonMap.size()>0) {
								 tax_name_per = "Charging cost "+String.valueOf(taxJsonMap.get("name").asText())+"("+String.valueOf(taxJsonMap.get("percnt").asText())+"%)";
								 taxAmount=currencySymbol+""+utils.decimalwithtwoZeros(new BigDecimal(String.valueOf(taxJsonMap.get("chargingAmount").asDouble())).setScale(2, RoundingMode.HALF_UP));
								 total_taxAmount = total_taxAmount.add(new BigDecimal(taxJsonMap.get("chargingAmount").asText()));
								 taxes tax = new taxes();
								 tax.setTax_name_per(tax_name_per);
								 tax.setTaxAmount(taxAmount);
								 taxes.add(tax);
							 }
							 if(taxJsonMap.size()>0) {
								 tax_name_per = "Idle fee cost "+String.valueOf(taxJsonMap.get("name").asText())+"("+String.valueOf(taxJsonMap.get("percnt").asText())+"%)";
								 taxAmount=currencySymbol+""+utils.decimalwithtwoZeros(new BigDecimal(String.valueOf(taxJsonMap.get("idleAmount").asDouble())).setScale(2, RoundingMode.HALF_UP));
								 total_taxAmount = total_taxAmount.add(new BigDecimal(taxJsonMap.get("idleAmount").asText()));
								 taxes tax = new taxes();
								 tax.setTax_name_per(tax_name_per);
								 tax.setTaxAmount(taxAmount);
								 taxes.add(tax);
							 }
						 }
					 }
					 if(!rewardType.equalsIgnoreCase("null") && rewardValue.doubleValue()>0) {
						 if(rewardType.equalsIgnoreCase("Amount")) {
							 taxes reward=new taxes();
							 String price =currencySymbol+String.valueOf(rewardValue.setScale(2, RoundingMode.HALF_UP)); 
							 reward.setTax_name_per("Reward balance");
							 reward.setTaxAmount(price);
							 taxes.add(reward);
						 }else if(rewardType.equalsIgnoreCase("kWh")){
							 taxes reward=new taxes();
							 String price =String.valueOf(rewardValue.setScale(4, RoundingMode.HALF_UP))+"kWh"; 
							 reward.setTax_name_per("Reward balance");
							 reward.setTaxAmount(price);
							 taxes.add(reward);
						 }
						
					 }
				 }
			}
			Map<String,Object> data = new HashMap<>();
			Map<String,Object> template = new HashMap<>();
			if(!String.valueOf(sessionData.get("txnType")).equalsIgnoreCase("PAYG")) {
				template.put("accName", String.valueOf(userObj.get("accountName")));
				template.put("digitalId", String.valueOf(userObj.get("digitalId")));
				template.put("user_accountBalance", currencySymbol+utils.roundToTwoDecimalPlaces(new BigDecimal(String.valueOf(userObj.get("accountBalance")))));
				template.put("user_type", "RegisteredUser");
				if(String.valueOf(sessionData.get("customerId")).contains("BCH")) {
					template.put("txn_by","Account balance");
				}else {
					template.put("txn_by","Account balance");
				}
			}else if(String.valueOf(sessionData.get("txnType")).equalsIgnoreCase("PAYG")){
				template.put("accName", "Customer");
				template.put("digitalId", String.valueOf(sessionData.get("customerId")));
				template.put("user_type", "PAYG");
				template.put("txn_by","Mobile Application");
			}
			template.put("phoneNumber", configs.get("phoneNumber"));
			template.put("portalUrl", configs.get("portalLink"));
			template.put("currDate", utils.userTimeZone(utils.getUTCDateString(), userTimeZoneId));
			template.put("stnAddress", stnObj.get("stationAddress"));
			template.put("stnRefNum", stnObj.get("stnRefNum"));
			template.put("sessionId", String.valueOf(sessionData.get("sessId")));
			template.put("sessionDuration", Utils.formatDuration(Double.parseDouble(String.valueOf(sessionData.get("sessionDuration")))));
			template.put("finalAmount", currencySymbol+utils.decimalwithtwoZeros(new BigDecimal(String.valueOf(sessionData.get("finalAmount")))));
			template.put("Idle_Time", "00:00:00");
			template.put("startSoc", sessionData.get("socStartVal"));
			template.put("endSoc", sessionData.get("socEndVal"));
			template.put("flatFee", "0.00");
			template.put("totalkWh", String.valueOf(new BigDecimal(String.valueOf(sessionData.get("totalkWh"))).setScale(4, RoundingMode.HALF_UP)));
			template.put("orgAddress", configs.get("address"));
			template.put("orgName", configs.get("orgName"));
			template.put("rateRider", rateRiderType+currencySymbol+utils.decimalwithtwodecimals(new BigDecimal(rateRiderAmount)));
			template.put("ratePer", rateRiderType+rateRiderPer);
			//template.put("taxAmount", currencySymbol+utils.decimalwithtwoZeros(new BigDecimal(taxAmount)));
			template.put("tax_name_per", tax_name_per);
			template.put("charging_cost", currencySymbol+String.valueOf(chargingCost.setScale(2, RoundingMode.HALF_UP)));
			
			template.put("site_name", siteObj.get("siteName"));
			template.put("capacity_ChargerType", stnObj.get("capacity") +" kW "+stnObj.get("chargerType"));
			template.put("start_time", utils.userTimeZone(String.valueOf(sessionData.get("startTimeStamp")),userTimeZoneId));
			template.put("end_time", utils.userTimeZone(String.valueOf(sessionData.get("endTimeStamp")),userTimeZoneId));
			template.put("prices",chargingPricing);
			template.put("support_mail", String.valueOf(configs.get("supportEmail")));
			template.put("support_phone", String.valueOf(configs.get("phoneNumber")));
			template.put("tax_amount", currencySymbol+utils.decimalwithtwodecimals(total_taxAmount));
			template.put("to_mail", sessionData.get("emailId"));
			template.put("taxes", taxes);
			template.put("idleCharging", idleCharging);
			
			data.put("template_data",template);
			data.put("from_mail_port", configs.get("port"));
			data.put("from_mail_host", configs.get("host"));
			data.put("from_mail", configs.get("email"));
			data.put("from_mail_auth", configs.get("email_auth"));
			data.put("from_mail_password", configs.get("password"));
			data.put("from_mail_protocol", configs.get("protocol"));
			data.put("subject", "BC Hydro EV charging session summary");
			data.put("to_mail", sessionData.get("emailId"));
			data.put("to_mail_cc", "");
			data.put("logo", configs.get("logo_url"));
			data.put("template_name", "chargingSummary.ftl");
			return emailServiceImpl.userEmail(data,resend,String.valueOf(sessionData.get("sessionId")));
		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	public long getTimeZone(String zone) {
		long zoneId=8;
		try {
			String query="select zone_id from zone where zone_name='"+zone+"'";
			List<Map<String,Object>> list=executeRepository.findAll(query);
			if(list.size()>0) {
				zoneId=Long.parseLong(String.valueOf(list.get(0).get("zone_id")));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return zoneId;
	}
	public String prices(String chargingPricing,String tariffId) {
		try {
			String query="select dt.text from tariff_in_display_text tdt inner join displayText dt on tdt.displayText_id=dt.id where tdt.tariff_id='"+tariffId+"' and dt.language='en'";
			List<Map<String,Object>> list= executeRepository.findAll(query);
			if(list.size()>0) {
				chargingPricing=String.valueOf(list.get(0).get("text"));
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return chargingPricing;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SessionImportedValues notifyChargingSessionSummaryData(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				boolean notifyAlert = false;
				if (siv.getTxnData() != null) {
//				JsonNode tariff = objectMapper.readTree(siv.getTxnData().getTariff_prices());
//				if(tariff.size() > 0) {
					if (!siv.isIdleBilling()) {
						if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
							PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
							if (pn != null && pn.isNotificationChargingCompleted()) {
								notifyAlert = true;
							}
						} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
							notifyAlert = true;
						}
					}
//				}
				}
				if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser") && notifyAlert) {
					PreferredNotification pn = preferredNotify(Long.valueOf(String.valueOf(siv.getUserObj().get("UserId").asLong())));
					if (pn != null && pn.isNotificationChargingCompleted()) {
						JsonNode stnJson = objectMapper.readTree(siv.getTxnData().getStn_obj());
						JSONObject map = new JSONObject();
						map.put("notificationId", utils.getRandomNumber(""));
						map.put("type", "Charging Summary");
						map.put("categoryIdentifier", "notification");
						map.put("energyDelivered", String.valueOf(siv.getTotalKwUsed()));
						map.put("finalCost", String.valueOf(siv.getFinalCostInslcCurrency()));
						map.put("referNo", siv.getStnRefNum());
						map.put("stationName", siv.getStnObj().get("stationName").asText());
						map.put("duration", String.valueOf(siv.getSessionDuration()));
						map.put("connectorId", stnJson.get("connector_id").asText());
						map.put("sessionId", siv.getChargeSessUniqId());
						map.put("currencyCode", siv.getUserObj().get("crncy_Code").asText());
						map.put("currencySymbol", siv.getUserObj().get("crncy_HexCode").asText());
						map.put("uuid", siv.getUserObj().get("uuid").asText());
						boolean flag = sendNotifications(Long.valueOf(String.valueOf(siv.getUserObj().get("UserId"))), "", "Charging Session", siv.getStnRefNum(), siv.getTxnData().getOrgId(), siv.getChargeSessUniqId(), map, siv.getChargeSessUniqId(), 0);
						siv.setNotification(flag);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return siv;
	}
//	@Override
//	public void updateNotificationAlert(SessionImportedValues siv,long count) {
//		try {
//			String query="update notification_tracker set emailflag='"+siv.isMail()+"',smsflag='"+siv.isSms()+"',pushNotificationFlag='"+siv.isNotification()+"' where sessionId='"+siv.getChargeSessUniqId()+"'";
//		    executeRepository.update(query);
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	@Override
	public SessionImportedValues smsChargingSessionSummaryData(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser") && !siv.isIdleBilling()) {
					PreferredNotification pn = preferredNotify(Long.valueOf(String.valueOf(siv.getUserObj().get("UserId").asLong())));
					if (pn != null && pn.isSmsChargingCompleted()) {
						JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
						if (userJson.size() > 0) {
							String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
							boolean flag = smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(), String.valueOf(siv.getChargeSessId()), "NrmlUserStop", Double.parseDouble(String.valueOf(siv.getFinalCostInslcCurrency())), phoneNumber, null, null, siv.getChargeSessUniqId(), 0);
							siv.setSms(flag);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return siv;
	}
	
	@Override
	public void lowBalanceMailAlert(SessionImportedValues siv,Double accBal) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isEmailLowWalletBalance()) {
					Map<String, Object> configs = configService.getAlertConfigs();
					Map<String,Object> data = new HashMap<>();
					Map<String,Object> template = new HashMap<>();
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					JsonNode siteObj = objectMapper.readTree(siv.getTxnData().getSite_obj());
					long userTimeZoneId = getTimeZone(siteObj.get("timeZone").asText());
					
					template.put("accName", String.valueOf(userJson.get("accountName").asText()));
					template.put("digitalId", String.valueOf(userJson.get("digitalId").asText()));
					template.put("currentBalance", "$"+accBal.toString());
					template.put("currDate", utils.userTimeZone(null,userTimeZoneId));
					template.put("stnRefNum", siv.getStnRefNum());
					template.put("sessionId", String.valueOf(siv.getChargeSessId()));
					template.put("phoneNumber", configs.get("phoneNumber"));
					template.put("orgAddress", configs.get("address"));
					template.put("orgName", configs.get("orgName"));
					template.put("portalUrl", configs.get("portalLink"));
					template.put("to_mail", String.valueOf(userJson.get("email").asText()));
					template.put("support_mail", String.valueOf(configs.get("supportEmail")));
					template.put("support_phone", String.valueOf(configs.get("phoneNumber")));
					
					data.put("from_mail_port", configs.get("port"));
					data.put("from_mail_host", configs.get("host"));
					data.put("from_mail", configs.get("email"));
					data.put("from_mail_auth", configs.get("email_auth"));
					data.put("from_mail_password", configs.get("password"));
					data.put("from_mail_protocol", configs.get("protocol"));
					data.put("subject", "Low Balance");
					data.put("to_mail", String.valueOf(userJson.get("email").asText()));
					data.put("to_mail_cc", "");
					data.put("logo", configs.get("logo_url"));
					data.put("template_name", "lowBalance.ftl");
					data.put("template_data", template);
					emailServiceImpl.userEmail(data,0,null);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean sendNotifications(Long userId, String message, String NoftyType,String stationRefNum, long orgId, String randomSessionId, JSONObject info,String sessionId,long resend) {
		try {
			Thread th = new Thread() {
				public void run() {
					try {
						List<DeviceDetails> deviceDetails = ocppUserService.getDeviceDetailsByUserId(userId);
						List<String> deviceTokens=new ArrayList();
						if (deviceDetails != null) {
							deviceDetails.forEach(device -> {
								try {
									if (device.getOrgId() != null && device.getOrgId().equals(orgId)) {
										if(!String.valueOf(device.getDeviceToken()).equalsIgnoreCase("")) {
											deviceTokens.add(device.getDeviceToken());
										}
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
						}
						if(deviceTokens.size()>0) {
							pushNotification.sendMulticastMessage(info, deviceTokens,sessionId,resend);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};
			th.start();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void lowBalancePushNotiAlert(SessionImportedValues siv) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isNotificationLowWalletBalance()) {
					JSONObject map = new JSONObject();
					map.put("type", "Low Balance");
					map.put("categoryIdentifier", "notification");
					map.put("notificationId", utils.getRandomNumber(""));
					map.put("uuid", siv.getUserObj().get("uuid").asText());
					map.put("currencySymbol", siv.getUserObj().get("crncy_HexCode").asText()); 
					map.put("currencyCode", siv.getUserObj().get("crncy_Code").asText()); 
					sendNotifications(siv.getUserObj().get("UserId").asLong(),"","Low Balance",siv.getStnRefNum(),siv.getTxnData().getOrgId(),siv.getChargeSessUniqId(),map,null,0);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void smsLowBalanceAlert(SessionImportedValues siv) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isNotificationLowWalletBalance()) {
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					if(userJson.size() > 0) {
						logger.info(Thread.currentThread().getId()+"userJson : "+userJson);
						String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
						String digitalId = String.valueOf(userJson.get("digitalId").asText());
						smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(),String.valueOf(siv.getChargeSessId()),"NrmlUserLowBalance",Double.parseDouble(String.valueOf(siv.getFinalCostInslcCurrency())),phoneNumber,digitalId,null,null,0);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void sessionInterruptedMailAlert(SessionImportedValues siv) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isEmailChargingInterrupted()) {
					Map<String, Object> configs = configService.getAlertConfigs();
					Map<String,Object> data = new HashMap<>();
					Map<String,Object> template = new HashMap<>();
					template.put("accName", siv.getUserObj().get("accountName").asText());
					template.put("currDate", utils.userTimeZone(null,siv.getSiteObj().get("timeZone").asLong()));
					template.put("stnRefNum", siv.getStnRefNum());
					template.put("sessionId", String.valueOf(siv.getChargeSessId()));
					template.put("phoneNumber", configs.get("phoneNumber"));
					template.put("orgAddress", configs.get("address"));
					template.put("orgName", configs.get("orgName"));
					template.put("portalUrl", configs.get("portalLink"));
					template.put("to_mail", String.valueOf(siv.getUserObj().get("email").asText()));
					template.put("support_mail", String.valueOf(configs.get("supportEmail")));
					template.put("support_phone", String.valueOf(configs.get("phoneNumber")));
					data.put("from_mail_port", configs.get("port"));
					data.put("from_mail_host", configs.get("host"));
					data.put("from_mail", configs.get("email"));
					data.put("from_mail_auth", configs.get("email_auth"));
					data.put("from_mail_password", configs.get("password"));
					data.put("from_mail_protocol", configs.get("protocol"));
					data.put("subject", "Charging Session Interrupted");
					data.put("to_mail", siv.getUserObj().get("email").asText());
					data.put("to_mail_cc", "");
					data.put("logo", configs.get("logo_url"));
					data.put("template_name", "sessionInterrupted.ftl");
					data.put("template_data", template);
					logger.info(Thread.currentThread().getId()+"mail template data : "+data);
					emailServiceImpl.userEmail(data,0,null);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	@Override
	public void sessionInterruptedPushNotifyAlert(SessionImportedValues siv) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isNotificationChargingInterrupted()) {
					JSONObject map = new JSONObject();
					map.put("type", "Session Interrupted");
					map.put("notificationId", utils.getRandomNumber(""));
					map.put("categoryIdentifier", "notification");
					map.put("sessionId", siv.getChargeSessUniqId());
					sendNotifications(siv.getUserObj().get("UserId").asLong(),"","Session Interrupted",siv.getStnRefNum(),siv.getTxnData().getOrgId(),siv.getChargeSessUniqId(),map,null,0);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void smsSessionInterruptedAlert(SessionImportedValues siv) {
		try {
			if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
				if(pn != null && pn.isSmsChargingInterrupted()) {
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					if(userJson.size() > 0) {
						String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
						smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(),String.valueOf(siv.getChargeSessId()),"NrmlUserSessInterup",Double.parseDouble(String.valueOf(siv.getFinalCostInslcCurrency())),phoneNumber,null,null,null,0);
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void alertPAYGStop(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG") && !siv.isIdleBilling()) {
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					if (userJson.size() > 0 && userJson.get("authorizeAmount").asDouble() > 0) {
						String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
						if (phoneNumber != null && !phoneNumber.contains("GUEST")) {
							smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(), String.valueOf(siv.getChargeSessId()), "PAYGStop", Double.parseDouble(String.valueOf(siv.getFinalCostInslcCurrency())), phoneNumber, null, null, null, 0);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void notifyInActiveBilling(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				boolean notifyAlert = false;
				boolean idleBilling = false;
				if (siv.getTxnData() != null && Double.parseDouble(String.valueOf(siv.getTotalKwUsed())) > siv.getTxnData().getMinkWhEnergy() && siv.isIdleBilling()) {
					idleBilling = true;
					if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
						PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
						if (pn != null && pn.isNotificationIdleBilling()) {
							notifyAlert = true;
						}
					} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
						notifyAlert = true;
					}
				}
				JSONArray iOSRecipients = new JSONArray();
				JSONArray androidRecipients = new JSONArray();
				String legacykey = "";
				String serverKey = "";
				String gracePeriod = "0";
				double idleFee = 0.0;
				double step = 0.0;
				List<String> deviceTokens = new ArrayList();
				if (notifyAlert) {
					String crncy_Code = String.valueOf(siv.getSiteObj().get("crncy_Code").asText());
					String crncy_HexCode = String.valueOf(siv.getSiteObj().get("crncy_HexCode").asText());
					logger.info(Thread.currentThread().getId() + "pricess : " + String.valueOf(objectMapper.readTree(siv.getTxnData().getTariff_prices()).get(0).get("cost_info").get(0).get("aditional")));
					JsonNode idleCharge = objectMapper.readTree(String.valueOf(objectMapper.readTree(String.valueOf(objectMapper.readTree(siv.getTxnData().getTariff_prices()).get(0).get("cost_info").get(0).get("aditional"))).get("idleCharge")));
					gracePeriod = idleCharge.get("gracePeriod").asText();
					idleFee = idleCharge.get("price").asDouble();
					step = idleCharge.get("step").asDouble();
					JSONObject map = new JSONObject();
					map.put("type", "Idle Billing");
					map.put("category", "Grace Time Started");
					map.put("billingStartTime", idleCharge.get("gracePeriod").asText());
					map.put("billingStartTimeLabel", "min");
					map.put("sessionId", siv.getChargeSessUniqId());
					map.put("idleFee", String.valueOf(idleFee));
					map.put("step", String.valueOf(step));
					map.put("currencyCode", crncy_Code);
					map.put("currencySymbol", crncy_HexCode);
					map.put("notificationId", utils.getRandomNumber(""));
					map.put("categoryIdentifier", "notification");

					Map<String, Object> configProperties = configService.configData(siv.getTxnData().getOrgId());
					if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
						List<DeviceDetails> deviceDetails = ocppUserService.getDeviceDetailsByUserId(siv.getUserObj().get("UserId").asLong());
						if (deviceDetails != null) {
							deviceDetails.forEach(device -> {
								try {
									if (!String.valueOf(device.getDeviceToken()).equalsIgnoreCase("")) {
										deviceTokens.add(device.getDeviceToken());
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
						}
//					deviceTokens.addAll(deviceDetails.stream().filter(device-> device.getDeviceType().equalsIgnoreCase("Android")).map(DeviceDetails::getDeviceToken).collect(Collectors.toList()));
//					deviceTokens.addAll(deviceDetails.stream().filter(device-> device.getDeviceType().equalsIgnoreCase("iOS")).map(DeviceDetails::getDeviceToken).collect(Collectors.toList()));
					} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
					}
					if (deviceTokens.size() > 0) {
						pushNotification.sendMulticastMessage(map, deviceTokens, null, 0);
					}
				}
				if (idleBilling) {
					JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
					if (userJson.size() > 0) {
						boolean smsIdleBilling = false;
						if (siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
							PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
							if (pn != null && pn.isSmsIdleBilling()) {
								smsIdleBilling = true;
							}
						} else if (siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")) {
							smsIdleBilling = true;
						}
						if (smsIdleBilling) {
							String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
							smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(), String.valueOf(siv.getChargeSessId()), "Chargingcomplete", idleFee, phoneNumber, step == 360 ? "hour" : "minute", String.valueOf(gracePeriod), null, 0);
						}
					}
					inActivityNotification(siv, deviceTokens, legacykey, serverKey, gracePeriod, notifyAlert, idleFee, step);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void inActivityNotification(SessionImportedValues siv,List<String> deviceTokens,String legacykey,String serverKey,String graceTime,boolean notifyAlert,double idleFee,double step) {
		try {
			Thread newThread = new Thread() {
				public void run() {
					try {
						boolean flag=true;
						double gracePeriod=Double.parseDouble(String.valueOf(graceTime));
						boolean notify=false;
						while(flag) {
							Thread.sleep(30000);
							gracePeriod=gracePeriod-0.5;
							if(gracePeriod<=0) {
								flag=false;
								notify=true;
							}
							String query="select id from  ocpp_TransactionData where sessionId='"+siv.getChargeSessUniqId()+"' and idleStatus!='faulted' and idleStatus!='Available'";
							List<Map<String,Object>> list=executeRepository.findAll(query);
							if(list.size()==0) {
								notify=false;
								flag=false;
							}
						}
						if(notify) {
							boolean sms=false;
							boolean email=false;
							if(siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser")) {
								PreferredNotification pn = preferredNotify(siv.getUserObj().get("UserId").asLong());
								if(pn != null && pn.isEmailIdleBilling()) {
									email = true;
								}
								if(pn != null && pn.isSmsIdleBilling()) {
									sms = true;
								}
							}else if(siv.getTxnData().getUserType().equalsIgnoreCase("PAYG")){
								email = true;
								sms = true;
							}
							
							if(email) {
								idleStartMail(siv);
							}
							JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
							  if(userJson.size() > 0 && sms) {
								String phoneNumber = String.valueOf(userJson.get("phoneNumber").asText());
								smsIntegrationImpl.sendSMSUsingBootConfg(siv.getStnRefNum(),String.valueOf(siv.getChargeSessId()),"idleFeeStart",idleFee,phoneNumber,step==360?"hour":"minute",String.valueOf(graceTime),null,0);
							  }
							if(notifyAlert) {
								String crncy_Code=String.valueOf(siv.getSiteObj().get("crncy_Code").asText());
								String crncy_HexCode=String.valueOf(siv.getSiteObj().get("crncy_HexCode").asText());
								JSONObject map = new JSONObject();
								map.put("type", "Idle Billing");
								map.put("category", "Grace Time Ended");
								map.put("billingStartTime", String.valueOf(graceTime));
								map.put("billingStartTimeLabel", "min");
								map.put("sessionId", siv.getChargeSessUniqId());
								map.put("idleFee", String.valueOf(idleFee));
								map.put("step", String.valueOf(step));
								map.put("currencyCode",crncy_Code);
								map.put("currencySymbol",crncy_HexCode );
								map.put("notificationId", utils.getRandomNumber(""));
								map.put("categoryIdentifier", "notification");
								
								if(deviceTokens.size()>0) {
									pushNotification.sendMulticastMessage(map, deviceTokens,null,0);
								}
							}
						}
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			};
			newThread.start();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void inActivityMail(SessionImportedValues siv) {
		try {
			Map<String, Object> configs = configService.getAlertConfigs();
			Map<String,Object> data = new HashMap<>();
			Map<String,Object> template = new HashMap<>();
			JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
			JsonNode siteObj = objectMapper.readTree(siv.getTxnData().getSite_obj());
			JsonNode tariffJson = objectMapper.readTree(siv.getTxnData().getTariff_prices());
			double gracePeriod = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("gracePeriod").asDouble();
			double idleFee = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("price").asDouble();
			double stepSize = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("step").asDouble();
			long userTimeZoneId = getTimeZone(siteObj.get("timeZone").asText());
			template.put("accName", String.valueOf(userJson.get("accountName").asText()));
			template.put("currDate", utils.userTimeZone(null,userTimeZoneId));
			template.put("gracePeriod", gracePeriod);
			template.put("idleFee", userJson.get("crncy_HexCode").asText()+utils.decimalwithtwoZeros(utils.decimalwithtwodecimals(new BigDecimal(String.valueOf(idleFee)))));
			template.put("stepSize", stepSize==360 ? "hour" : "minute");
			template.put("stnRefNum", siv.getStnRefNum());
			template.put("sessionId", String.valueOf(siv.getChargeSessId()));
			template.put("phoneNumber", configs.get("phoneNumber"));
			template.put("orgAddress", configs.get("address"));
			template.put("orgName", configs.get("orgName"));
			template.put("portalUrl", configs.get("portalLink"));
			template.put("to_mail", String.valueOf(userJson.get("email").asText()));
			template.put("support_mail", String.valueOf(configs.get("supportEmail")));
			template.put("support_phone", String.valueOf(configs.get("phoneNumber")));
			
			data.put("template_data",template);
			data.put("from_mail_port", configs.get("port"));
			data.put("from_mail_host", configs.get("host"));
			data.put("from_mail", configs.get("email"));
			data.put("from_mail_auth", configs.get("email_auth"));
			data.put("from_mail_password", configs.get("password"));
			data.put("from_mail_protocol", configs.get("protocol"));
			data.put("subject", "BC Hydro EV Charging complete");
			data.put("to_mail", String.valueOf(userJson.get("email").asText()));
			data.put("to_mail_cc", "");
			data.put("logo", configs.get("logo_url"));
			data.put("template_name", "idleCharge.ftl");
			emailServiceImpl.userEmail(data,0,null);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void idleStartMail(SessionImportedValues siv) {
		try {
			Map<String, Object> configs = configService.getAlertConfigs();
			Map<String,Object> data = new HashMap<>();
			Map<String,Object> template = new HashMap<>();
			JsonNode userJson = objectMapper.readTree(siv.getTxnData().getUser_obj());
			JsonNode siteObj = objectMapper.readTree(siv.getTxnData().getSite_obj());
			JsonNode tariffJson = objectMapper.readTree(siv.getTxnData().getTariff_prices());
			double gracePeriod = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("gracePeriod").asDouble();
			double idleFee = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("price").asDouble();
			double stepSize = objectMapper.readTree(String.valueOf(tariffJson.get(0).get("cost_info").get(0).get("aditional").get("idleCharge"))).get("step").asDouble();
			long userTimeZoneId = getTimeZone(siteObj.get("timeZone").asText());
			template.put("accName", String.valueOf(userJson.get("accountName").asText()));
			template.put("currDate", utils.userTimeZone(null,userTimeZoneId));
			template.put("gracePeriod", gracePeriod);
			template.put("idleFee", userJson.get("crncy_HexCode").asText()+utils.decimalwithtwoZeros(utils.decimalwithtwodecimals(new BigDecimal(String.valueOf(idleFee)))));
			template.put("stepSize", stepSize==360 ? "hour" : "minute");
			template.put("stnRefNum", siv.getStnRefNum());
			template.put("sessionId", String.valueOf(siv.getChargeSessId()));
			template.put("phoneNumber", configs.get("phoneNumber"));
			template.put("orgAddress", configs.get("address"));
			template.put("orgName", configs.get("orgName"));
			template.put("portalUrl", configs.get("portalLink"));
			template.put("to_mail", String.valueOf(userJson.get("email").asText()));
			template.put("support_mail", String.valueOf(configs.get("supportEmail")));
			template.put("support_phone", String.valueOf(configs.get("phoneNumber")));
			
			data.put("template_data",template);
			data.put("from_mail_port", configs.get("port"));
			data.put("from_mail_host", configs.get("host"));
			data.put("from_mail", configs.get("email"));
			data.put("from_mail_auth", configs.get("email_auth"));
			data.put("from_mail_password", configs.get("password"));
			data.put("from_mail_protocol", configs.get("protocol"));
			data.put("subject", "BC Hydro EV Idle Fee start");
			data.put("to_mail", String.valueOf(userJson.get("email").asText()));
			data.put("to_mail_cc", "");
			data.put("logo", configs.get("logo_url"));
			data.put("template_name", "idleFeeStart.ftl");
			emailServiceImpl.userEmail(data,0,null);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
    public SessionImportedValues chargingActivityMail(SessionImportedValues siv) {
		if (!siv.getStTxnObj().isOfflineFlag()) {
			try {
				if (siv.getTxnData() != null) {
//				JsonNode tariff = objectMapper.readTree(siv.getTxnData().getTariff_prices());
//				if(tariff.size() > 0) {
//					JsonNode prices = objectMapper.readTree(String.valueOf(tariff.get(0).get("cost_info")));
//					if(prices.size() > 0 && !siv.isIdleBilling()) {
//						mailChargingSessionSummaryData(siv.getChargeSessUniqId(),false);
//					}
//				}
					if (!siv.isIdleBilling()) {
						String response = mailChargingSessionSummaryData(siv.getChargeSessUniqId(), false);
						siv.setMail(response.equalsIgnoreCase("success"));
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	return siv;
    }
	@Override
	public boolean chargingActivityNotification(OCPPTransactionData txnData,long userId,double kwhUsed,double finalCost,double duration) {
		try {
			if(txnData.getUserType().equalsIgnoreCase("RegisteredUser")) {
				PreferredNotification pn = preferredNotify(userId);
				if(pn != null && pn.isNotificationChargingCompleted()) {
					JsonNode stnJson = objectMapper.readTree(txnData.getStn_obj());
					JsonNode userObj = objectMapper.readTree(txnData.getUser_obj());
					JSONObject map = new JSONObject();
					map.put("notificationId", utils.getRandomNumber(""));
					map.put("type", "Charging Summary");
					map.put("categoryIdentifier", "notification");
					map.put("energyDelivered", String.valueOf(kwhUsed));
					map.put("finalCost", String.valueOf(finalCost)); 
					map.put("referNo",  stnJson.get("referNo").asText()); 
					map.put("stationName", stnJson.get("stationName").asText());
					map.put("duration", String.valueOf(duration));
					map.put("connectorId", stnJson.get("connector_id").asText());
					map.put("sessionId", txnData.getSessionId());
					map.put("currencyCode", userObj.get("crncy_Code").asText());
					map.put("currencySymbol", userObj.get("crncy_HexCode").asText());
					map.put("uuid", userObj.get("uuid").asText());
					boolean flag=sendNotifications(userId,"","Charging Session",stnJson.get("referNo").asText(),txnData.getOrgId(),txnData.getSessionId(),map,txnData.getSessionId(),0);
					return flag;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
