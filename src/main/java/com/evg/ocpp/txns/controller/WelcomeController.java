package com.evg.ocpp.txns.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.evg.ocpp.txns.forms.ResponseMessage;

@Controller
public class WelcomeController {

	private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ResponseEntity<ResponseMessage> welcome() {
		String msg = "Welcome To EVG-Transactions Service";

		logger.info("WelcomeController.welcome() - msg [" + msg + "]");

		return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(msg));
	}

}