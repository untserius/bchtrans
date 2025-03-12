package com.evg.ocpp.txns.Service;

import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;

public interface OfflineTransactionHandlerService {

    SessionImportedValues handleOfflineTransaction(SessionImportedValues siv, OCPPStartTransaction startTxnObj,
                                                          MeterValues meterValuesObj);

    SessionImportedValues handleOfflineStopTransaction(SessionImportedValues siv, OCPPStartTransaction startTxnObj,
                                                              StopTransaction stopTxnObj);

}
