package com.evg.ocpp.txns.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.evg.ocpp.txns.Service.alertsService;
import com.evg.ocpp.txns.forms.NotificationTrackerForm;
import com.evg.ocpp.txns.forms.ResponseMessage;

@RestController
@RequestMapping(value="/actions")
public class sessionController {
	
	private final static Logger logger = LoggerFactory.getLogger(sessionController.class);
	
	@Autowired
	private alertsService alertsService;

	@RequestMapping(value = "/sessionSummaryMail", method = RequestMethod.POST)
	public String chargingSessionsSummaryMail(@RequestBody Map<String,Object> map) {
		logger.info("/actions/sessionSummaryMail : "+map);
		return alertsService.mailChargingSessionSummaryData(String.valueOf(map.get("sessionId")),false);
	}
	
	@RequestMapping(value = "/notification", method = RequestMethod.POST)
	public ResponseMessage notifications(@RequestBody NotificationTrackerForm map) {
		logger.info("/actions/notification : "+map);
		ResponseMessage responce=new ResponseMessage();
		if(map.getType().equalsIgnoreCase("ChargingActivity")) {
			try {
				responce.setStatusCode(200);
				alertsService.notification(map.getSessionId(),map.getNotifyType());
			} catch (Exception e) {
				responce.setStatusCode(500);
			}
		}
		return responce;
	}
}
