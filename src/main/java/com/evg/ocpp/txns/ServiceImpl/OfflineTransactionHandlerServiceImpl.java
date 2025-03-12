package com.evg.ocpp.txns.ServiceImpl;

import com.evg.ocpp.txns.Service.OfflineTransactionHandlerService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.MeterValues;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.forms.StopTransaction;
import com.evg.ocpp.txns.model.ocpp.OCPPStartTransaction;
import com.evg.ocpp.txns.model.ocpp.OfflineTransactionFlags;
import com.evg.ocpp.txns.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class OfflineTransactionHandlerServiceImpl implements OfflineTransactionHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(OfflineTransactionHandlerServiceImpl.class);

    @Autowired
    private com.evg.ocpp.txns.Service.ocppMeterValueService ocppMeterValueService;

    @Autowired
    private Utils utils;

    @Autowired
    private GeneralDao<?, ?> generalDao;

    @Override
    public SessionImportedValues handleOfflineTransaction(SessionImportedValues siv, OCPPStartTransaction startTxnObj,
                                                          MeterValues meterValuesObj) {
        try {
            if (startTxnObj.isOfflineFlag()) {
                Date currentMeterValueTime = meterValuesObj.getMeterValues().get(0).getTimestamp();

                // Skip if current timestamp is offline
                if (utils.getOfflineFlag(currentMeterValueTime)) {
                    logger.info("Current meter value timestamp is offline, skipping: " + currentMeterValueTime);
                    siv.setMeterValueTimeStatmp(currentMeterValueTime);
                    return siv;  // Continue with original timestamps
                }

                // Check if we already have set the start time flag for this transaction
                boolean startTimeAlreadySet = checkStartTimeFlag(siv.getChargeSessUniqId());

                if (!startTimeAlreadySet) {
                    // First valid meter value - set start timestamp
                    logger.info("First valid meter value, setting start time to: " + currentMeterValueTime);
                    siv.setStartTransTimeStamp(currentMeterValueTime);
                    updateStartTransactionTimestamp(siv.getChargeSessUniqId(), currentMeterValueTime, startTxnObj);

                    // Set the flag in database
                    setStartTimeFlag(siv.getChargeSessUniqId());
                }

                // Always check timestamp sequence for meter value timestamp
                Date previousEndTime = (Date) ocppMeterValueService.getPreviousSessionData(siv.getChargeSessUniqId()).get("endTimeStamp");

                if (previousEndTime != null && !currentMeterValueTime.after(previousEndTime)) {
                    // Out of sequence - use previous timestamp
                    logger.info("Out of sequence timestamp detected. Current: " + currentMeterValueTime +
                            ", Previous: " + previousEndTime);
                    siv.setMeterValueTimeStatmp(previousEndTime);
                } else {
                    // In sequence - use current timestamp
                    siv.setMeterValueTimeStatmp(currentMeterValueTime);
                }
            } else {
                // For online transactions, use normal timestamps
                siv.setStartTransTimeStamp(startTxnObj.getTimeStamp());
                siv.setMeterValueTimeStatmp(meterValuesObj.getMeterValues().get(0).getTimestamp());
            }
            return siv;
        } catch (Exception e) {
            logger.error("Error handling offline transaction", e);
            return siv;  // Continue with original timestamps
        }
    }

    private boolean checkStartTimeFlag(String sessionId) {
        try {
            // Check if flag exists using HQL
            String hql = "FROM OfflineTransactionFlags WHERE sessionId = '" + sessionId + "'";
            OfflineTransactionFlags flag = new OfflineTransactionFlags();
            flag = generalDao.findOne(hql, flag);
            return flag != null;
        } catch (Exception e) {
            logger.error("Error checking start time flag", e);
            return false;
        }
    }

    private void setStartTimeFlag(String sessionId) {
        try {
            // Create and save flag entity
            OfflineTransactionFlags flag = new OfflineTransactionFlags();
            flag.setSessionId(sessionId);
            flag.setStartTimeSet(true);
            flag.setCreatedAt(new Date());
            generalDao.save(flag);
            logger.info("Set offline transaction flag for session: " + sessionId);
        } catch (Exception e) {
            logger.error("Error setting start time flag", e);
        }
    }

    @Override
    public SessionImportedValues handleOfflineStopTransaction(SessionImportedValues siv, OCPPStartTransaction startTxnObj,
                                                              StopTransaction stopTxnObj) {
        try {
            if (startTxnObj.isOfflineFlag()) {
                // Check if we've tracked any valid meter values
                boolean startTimeAlreadySet = checkStartTimeFlag(siv.getChargeSessUniqId());

                if (!startTimeAlreadySet) {
                    // No valid meter values were recorded, use transaction timestamps
                    siv.setStartTransTimeStamp(startTxnObj.getTimeStamp());
                    siv.setMeterValueTimeStatmp(stopTxnObj.getTimeStamp());
                    logger.info("No valid meter values received, using start and stop transaction timestamps");
                } else {
                    // We had valid meter values, use the last recorded meter value timestamp
                    Date lastMeterValueTimestamp = getLastMeterValueTimestamp(siv.getChargeSessUniqId());

                    if (lastMeterValueTimestamp != null) {
                        // Only override stop timestamp if it's BEFORE the last meter value
                        if (stopTxnObj.getTimeStamp().before(lastMeterValueTimestamp)) {
                            siv.setMeterValueTimeStatmp(lastMeterValueTimestamp);
                            logger.info("Stop timestamp is before last meter value, using last meter value: " + lastMeterValueTimestamp);
                        } else {
                            // Stop timestamp is valid (after last meter value) - use it
                            siv.setMeterValueTimeStatmp(stopTxnObj.getTimeStamp());
                            logger.info("Using valid stop transaction timestamp: " + stopTxnObj.getTimeStamp());
                        }
                    }
                }

                // Clean up the flag record
                deleteStartTimeFlag(siv.getChargeSessUniqId());
            }

            return siv;
        } catch (Exception e) {
            logger.error("Error handling offline stop transaction", e);
            return siv;
        }
    }

    private Date getLastMeterValueTimestamp(String sessionId) {
        try {
            String sql = "SELECT endTimeStamp FROM session WHERE sessionId = '" + sessionId + "'";
            List<Map<String, Object>> result = generalDao.getMapData(sql);

            if (result != null && !result.isEmpty() && result.get(0).get("endTimeStamp") != null) {
                return utils.stringToDate(String.valueOf(result.get(0).get("endTimeStamp")));
            }
            return null;
        } catch (Exception e) {
            logger.error("Error getting last meter value timestamp", e);
            return null;
        }
    }

    private void deleteStartTimeFlag(String sessionId) {
        try {
            // Delete flag using HQL
            String hql = "DELETE FROM OfflineTransactionFlags WHERE sessionId = '" + sessionId + "'";
            generalDao.updateHqlQuiries(hql);
            logger.info("Deleted offline transaction flag for session: " + sessionId);
        } catch (Exception e) {
            logger.error("Error deleting start time flag", e);
        }
    }

    private void updateStartTransactionTimestamp(String sessionId, Date newTimestamp, OCPPStartTransaction startTxnObj) {
        try {
            // Get the OCPPStartTransaction entity directly using sessionId
            String hql = "FROM OCPPStartTransaction WHERE sessionId = '" + sessionId + "'";
            OCPPStartTransaction startTxn = new OCPPStartTransaction();
            startTxn = generalDao.findOne(hql, startTxn);

            if (startTxn != null) {
                // Update the timestamp
                startTxnObj.setTimeStamp(newTimestamp);
                startTxn.setTimeStamp(newTimestamp);
                generalDao.update(startTxn);
                logger.info("Updated start transaction timestamp in database for session: " + sessionId +
                        " to: " + newTimestamp);
            } else {
                logger.warn("Could not find OCPPStartTransaction for sessionId: " + sessionId);
            }
        } catch (Exception e) {
            logger.error("Error updating start transaction timestamp", e);
        }
    }
}
