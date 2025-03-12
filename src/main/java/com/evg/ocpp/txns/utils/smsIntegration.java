package com.evg.ocpp.txns.utils;

import java.text.DecimalFormat;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.ocppUserService;
import com.evg.ocpp.txns.config.EmailServiceImpl;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


@Service
public class smsIntegration {
	static Logger logger = LoggerFactory.getLogger(smsIntegration.class);
	@Value("${sms.account_sid}")
	String sms_account_sid;
	@Value("${sms.auth_token}")
	String sms_auth_token;
	@Value("${sms.from_number}")
	String sms_from_phone;
	@Autowired
	private LoggerUtil customeLogger;
	
	@Autowired
	private ocppUserService userService;
	
	@Value("${symbol.currency}")
	private String currency;
    
    public String sendSMSToUser(final HashMap<String, String> reqObjMap) {
        String toPhoneNo = null;
        String sendTextmessage = null;
        String responseMesg = null;
        String stationId = null;
        try {
            if (reqObjMap != null && !reqObjMap.isEmpty()) {
                if (reqObjMap.containsKey("toPhoneNo")) {
                    toPhoneNo = reqObjMap.get("toPhoneNo");
                }
                if (reqObjMap.containsKey("smsMessage")) {
                    sendTextmessage = reqObjMap.get("smsMessage");
                }
                if (reqObjMap.containsKey("stationId")) {
                    stationId = reqObjMap.get("stationId");
                }
                logger.info("53>>>stationId>>>" + stationId + ">>>toPhoneNo>>>" + toPhoneNo + ">>>sendTextmessage>>>>" + sendTextmessage);
                if (toPhoneNo != null && sendTextmessage != null && stationId != null) {
                    Twilio.init(sms_account_sid, sms_auth_token);
                    try {
                        final Message message = (Message)Message.creator(new PhoneNumber(toPhoneNo), new PhoneNumber(sms_from_phone), sendTextmessage).create();
                        logger.info("SMS Send Successfully....");
                        responseMesg = "success";
                    }
                    catch (Exception e) {
                        logger.error("",e);
                        responseMesg = "fail";
                        logger.error(String.valueOf(new StringBuilder().append(e)));
                    }
                }
                else {
                    responseMesg = "fail";
                }
            }
        } catch (Exception e) {
            //logger.error("",e);
            responseMesg = "fail";
            //LOGGER.error(String.valueOf(new StringBuilder().append(e)));
        }
        logger.info("71>>>responseMesg>>>" + responseMesg);
        return responseMesg;
    }
    
    public boolean sendSMSUsingBootConfg(String stationId,String sessionId,String msgType,Double amount,String toNumber,String digitalId,String graceTime,String randomSessionId,long resend) {
    	int letterCnt = toNumber.length();
    	final String toNum = toNumber;
    	if(letterCnt > 8) {
			if(!toNumber.substring(0, 2).equalsIgnoreCase("+1")) {
				toNumber = "+1".concat(toNumber);
			}
    	}
    	try {
    		Thread th = new Thread() {
        		public void run() {
        			String messageFormat = "";
        	    	customeLogger.info(stationId, "smsSendingUsingBootConfg started.......");
        			try {
        				if(letterCnt > 8) {
        					String appName = "BC Hydro";
        					String revenueStr = "";
        					if(msgType != null && msgType.equals("NrmlUserLowBalance")){
        						messageFormat = "Your "+appName+" EV Charging Account "+digitalId+" is low on balance. Please top up to avoid service interruption.";
        					}else if(msgType != null && msgType.equals("NrmlUserStop")){
        						DecimalFormat df1 = new DecimalFormat("##0.00");
        						revenueStr = df1.format(amount);
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Your charging session has stopped. Total charging cost is "+currency+""+revenueStr;
        					}else if(msgType != null && msgType.equals("NrmlUserSessInterup")){
        						DecimalFormat df1 = new DecimalFormat("##0.00");
        						revenueStr = df1.format(amount);
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Your charging session was interrupted. Please check the charger connector and try again.";
        					} else if(msgType != null && msgType.equals("PAYGStop")){
        						DecimalFormat df1 = new DecimalFormat("##0.00");
        						revenueStr = df1.format(amount);
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+" . Your charging session (ID "+sessionId+") has ended. Total session cost is $"+revenueStr+" Pre-authorized funds will be released";
        					}else if(msgType != null && msgType.equals("GuestStart")){
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Your charging session has started. We’ve placed an authorization hold of "+currency+"40.00 on your card that will be released at the end of the transaction.";
        					}else if(msgType != null && msgType.equals("Chargingcomplete")){
        						DecimalFormat df1 = new DecimalFormat("##0.00");
        						revenueStr = df1.format(amount);
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Your EV has finished charging. Please disconnect and move your vehicle so others can use this charger. You have "+graceTime+" minutes to disconnect before being charged a "+currency+revenueStr+" per "+digitalId+" idle fee.";
        					}else if(msgType != null && msgType.equals("idleFeeStart")){
        						DecimalFormat df1 = new DecimalFormat("##0.00");
        						revenueStr = df1.format(amount);
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Your "+graceTime+" minute grace period has ended and we’ve started charging you idle fees at "+currency+revenueStr+" per "+digitalId+" (before tax). Please disconnect and move your vehicle to avoid further charges.";
        					}else if(msgType != null && msgType.equals("stop")){
        						messageFormat = ""+appName+" Charging Charger ID "+stationId+". Thank you for charging with BC Hydro.  Check your email or our EV app for your charging activity summary.";
        					}
        					customeLogger.info(stationId, "SMS messageFormat : "+messageFormat);
        					HashMap<String,String> reqObjMap = new HashMap<String,String>();
        					reqObjMap.put("toPhoneNo", toNum);
        					reqObjMap.put("smsMessage", messageFormat);
        					reqObjMap.put("stationId", stationId);
        					sendSMSToUser(reqObjMap);
        					customeLogger.info(stationId,"smsSendingUsingBootConfg success .......");
        					
        					if(sessionId!=null && !sessionId.equalsIgnoreCase("null")) {
    							userService.updateNotification("smsflag",true,randomSessionId,resend);
    						}
        				}else {
        					customeLogger.info(stationId,"Invalid Phone Number to send SMS .......");
        				}
        			} catch (Exception e) {
        				logger.error("",e);
        				customeLogger.info(stationId,"smsSendingUsingBootConfg failed .......");
        			}
        			customeLogger.info(stationId,"smsSendingUsingBootConfg ended .......");
        		}
        	};
        	th.start();
    	}catch (Exception e) {
			logger.error("",e);
			return false;
		}
    	return true;
    }
}
