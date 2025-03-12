package com.evg.ocpp.txns.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.forms.FinalData;
import com.evg.ocpp.txns.forms.TransactionForm;
import com.evg.ocpp.txns.utils.JSONDataParser;
import com.evg.ocpp.txns.webSocket.MeterValuesService;
import com.evg.ocpp.txns.webSocket.TransactionService;

@Service
public class MessageService {
	
	@Autowired
	private JSONDataParser jsonDataParser;
	
	@Autowired
	private MeterValuesService meterValuesService;
	
	@Autowired
	private TransactionService transactionService;
	
	public void messageHandle(TransactionForm form) {
		Thread newThread = new Thread() {
			public void run() {
				try {
					if(form.getMessage()!=null && !form.getMessage().equalsIgnoreCase("")) {
					   FinalData finalData = jsonDataParser.getData(form.getMessage(), form.getClientId());
					   if(form.getMessageType().equalsIgnoreCase("MeterValue") && finalData.getMeterValues()!=null) {
						   meterValuesService.meterVal(finalData, form.getClientId(), form.getMessage());
					   }else if(form.getMessageType().equalsIgnoreCase("StopTransaction") && finalData.getStopTransaction()!=null) {
						   transactionService.stopTransaction(finalData, form.getClientId(), form.getMessage(),form.getStnId());
					   }
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		};
		newThread.start();
	}

}
