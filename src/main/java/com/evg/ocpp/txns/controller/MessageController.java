package com.evg.ocpp.txns.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.evg.ocpp.txns.Service.MessageService;
import com.evg.ocpp.txns.Service.ocppMeterValueService;
import com.evg.ocpp.txns.forms.ResponseMessage;
import com.evg.ocpp.txns.forms.TransactionForm;

@RestController
@RequestMapping(value="/transaction")
public class MessageController {
	
	private final static Logger logger = LoggerFactory.getLogger(MessageController.class);
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private ocppMeterValueService ocppMeterValueService;
	
	@RequestMapping(value = "/request", method = RequestMethod.POST)
	public ResponseMessage RestAPI(@RequestBody TransactionForm form) {
		ResponseMessage response = new ResponseMessage();
		try {
			messageService.messageHandle(form);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
	
	@RequestMapping(value = "/idlebilling", method = RequestMethod.POST)
	public ResponseMessage chargingSessionsSummaryMail(@RequestBody Map<String,Object> map) {
		ResponseMessage response = new ResponseMessage();
		try {
			ocppMeterValueService.idleBilling(String.valueOf(map.get("sessionId")), String.valueOf(map.get("time")));
			response.setStatus("seccess");
			response.setStatusCode(200);
		}catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
