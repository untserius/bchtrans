package com.evg.ocpp.txns.ServiceImpl;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.EVGSearchService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.forms.SessionImportedValues;
import com.evg.ocpp.txns.model.es.OCPPChargingIntervalData;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.repository.es.EVGRepositoryChargingInterval;
import com.evg.ocpp.txns.utils.EsLoggerUtil;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class chargingIntervalServiceImpl {
	
	private final static Logger logger = LoggerFactory.getLogger(chargingIntervalServiceImpl.class);

	@Autowired
	private EsLoggerUtil esLoggerUtil;
	
	@Autowired
	private ExecuteRepository executeRepository;

	@Autowired
	private Utils utils;

	@Autowired
	private EVGRepositoryChargingInterval chargingInterval;
	
	public OCPPChargingIntervalData findbySessionId(String sessionId) throws ParseException {
		OCPPChargingIntervalData repositoryChargingInterval= null;
		try {
		Pageable pageable = PageRequest.of(0, 1, Sort.by("endInterval").descending());

		if (!sessionId.equalsIgnoreCase("null")) {
			Page<OCPPChargingIntervalData> data= chargingInterval.findBySessionId(sessionId ,pageable);
			List<OCPPChargingIntervalData> list = data.getContent();
			if(list.size()>0) {
				repositoryChargingInterval=list.get(0);
			}
		} 
		}catch (Exception e) {
			e.printStackTrace();
		}
		return repositoryChargingInterval;

	}

	public void chargingIntervalDataLogs(SessionImportedValues siv) {
		try {
			String stnRefNum = siv.getStnRefNum();
			OCPPChargingIntervalData ocppChargingIntervalDataList = findbySessionId(String.valueOf(siv.getSession().getId()));
			Long userId = 0l;
			if(siv.getTxnData().getUserType() != null && siv.getTxnData().getUserType().equalsIgnoreCase("RegisteredUser") ) {
				userId =String.valueOf(siv.getUserObj().get("UserId").asText()).equalsIgnoreCase("null") ? 0 : siv.getUserObj().get("UserId").asLong();
			}
			if (ocppChargingIntervalDataList == null) {

				OCPPChargingIntervalData ocppChargingIntervalData = new OCPPChargingIntervalData();
				ocppChargingIntervalData.setId(utils.getuuidRandomId(siv.getStnId()));
				ocppChargingIntervalData.setStationId(siv.getStnId());
				ocppChargingIntervalData.setPortId(siv.getPortId());
				ocppChargingIntervalData.setSessionId(String.valueOf(siv.getSession().getId()));
				ocppChargingIntervalData.setStartTimestamp(siv.getStartTransTimeStamp());
				ocppChargingIntervalData.setEndTimestamp(siv.getMeterValueTimeStatmp());
				ocppChargingIntervalData.setNextIntervalTimestamp(nextIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
				ocppChargingIntervalData.setIdTag(siv.getSession().getCustomerId());
				ocppChargingIntervalData.setPortPriceUnit(siv.getVendingUnit());
				ocppChargingIntervalData.setPortPrice(siv.getVendingPrice());
				ocppChargingIntervalData.setReasonForTermination(siv.getReasonForTermination());
				ocppChargingIntervalData.setUserId(userId);
				ocppChargingIntervalData.setkWUsed(siv.getTotalKwUsed().doubleValue());
				ocppChargingIntervalData.setSpent(siv.getFinalCosttostore());
				ocppChargingIntervalData.setTransactionCost(siv.getFinalCostInslcCurrency());
				ocppChargingIntervalData.setIntervalDuration(siv.getSessionDuration().doubleValue());
				ocppChargingIntervalData.setPowerActiveImport(siv.getSession().getPowerActiveImport_value());
				ocppChargingIntervalData.setStartInterval(startIntervalTime(siv.getStartTransTimeStamp(), stnRefNum));
				ocppChargingIntervalData.setEndInterval(endIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
				esLoggerUtil.createOCPPChargingIntervalDataIndex(ocppChargingIntervalData);
			} else {
				OCPPChargingIntervalData ocppChargingIntervalData = ocppChargingIntervalDataList;
				ocppChargingIntervalData.setStationId(siv.getStnId());
				ocppChargingIntervalData.setPortId(siv.getPortId());
				ocppChargingIntervalData.setSessionId(String.valueOf(siv.getSession().getId()));
				ocppChargingIntervalData.setIdTag(siv.getIdTag());
				ocppChargingIntervalData.setPortPriceUnit(siv.getVendingUnit());
				ocppChargingIntervalData.setPortPrice(siv.getVendingPrice());
				ocppChargingIntervalData.setReasonForTermination(siv.getReasonForTermination());
				ocppChargingIntervalData.setUserId(userId);

				Date intervalDateEs = ocppChargingIntervalData.getNextIntervalTimestamp();
				Date endTimeEs = siv.getMeterValueTimeStatmp();

				Double intervalDuration = siv.getSessionDuration().doubleValue() - Double.valueOf(String.valueOf(siv.getPreviousSessionData().get("sessionElapsedInMin")));
				Double intervalKwh = siv.getTotalKwUsed().doubleValue() - Double.valueOf(String.valueOf(siv.getPreviousSessionData().get("kilowattHoursUsed")));

				ocppChargingIntervalData.setEndInterval(endIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
				if (intervalDateEs.compareTo(endTimeEs) == 0) {

					ocppChargingIntervalData.setkWUsed(ocppChargingIntervalData.getkWUsed() + intervalKwh);
					ocppChargingIntervalData.setSpent(siv.getFinalCosttostore());
					ocppChargingIntervalData.setTransactionCost(siv.getFinalCostInslcCurrency());
					ocppChargingIntervalData.setIntervalDuration(ocppChargingIntervalData.getIntervalDuration() + intervalDuration);
					ocppChargingIntervalData.setStartTimestamp(ocppChargingIntervalData.getStartTimestamp());
					ocppChargingIntervalData.setEndTimestamp(siv.getMeterValueTimeStatmp());
					ocppChargingIntervalData.setNextIntervalTimestamp(nextIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
					ocppChargingIntervalData.setPowerActiveImport(siv.getSession().getPowerActiveImport_value());
				} else if (intervalDateEs.compareTo(endTimeEs) > 0) {

					ocppChargingIntervalData.setkWUsed(ocppChargingIntervalData.getkWUsed() + intervalKwh);
					ocppChargingIntervalData.setSpent(siv.getFinalCosttostore());
					ocppChargingIntervalData.setTransactionCost(siv.getFinalCostInslcCurrency());
					ocppChargingIntervalData.setIntervalDuration(ocppChargingIntervalData.getIntervalDuration() + intervalDuration);
					ocppChargingIntervalData.setStartTimestamp(ocppChargingIntervalData.getStartTimestamp());
					ocppChargingIntervalData.setEndTimestamp(siv.getMeterValueTimeStatmp());
					ocppChargingIntervalData.setNextIntervalTimestamp(nextIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
					ocppChargingIntervalData.setPowerActiveImport(siv.getSession().getPowerActiveImport_value());
					esLoggerUtil.createOCPPChargingIntervalDataIndex(ocppChargingIntervalData);
				} else if (intervalDateEs.compareTo(endTimeEs) < 0) {
					ocppChargingIntervalData.setId(utils.getuuidRandomId(siv.getStnId()));
					ocppChargingIntervalData.setkWUsed(intervalKwh);
					ocppChargingIntervalData.setSpent(siv.getFinalCosttostore());
					ocppChargingIntervalData.setTransactionCost(siv.getFinalCostInslcCurrency());
					ocppChargingIntervalData.setIntervalDuration(intervalDuration);
					ocppChargingIntervalData.setStartTimestamp(ocppChargingIntervalData.getEndTimestamp());
					ocppChargingIntervalData.setEndTimestamp(siv.getMeterValueTimeStatmp());
					ocppChargingIntervalData.setNextIntervalTimestamp(nextIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
					ocppChargingIntervalData.setStartInterval(startIntervalTime(siv.getMeterValueTimeStatmp(), stnRefNum));
					//chargingIntervalData.setEndInterval(endIntervalTime(sessionImportedValues.getMeterValueTimeStatmp(),stnRefNum));
					ocppChargingIntervalData.setPowerActiveImport(siv.getSession().getPowerActiveImport_value());
					esLoggerUtil.createOCPPChargingIntervalDataIndex(ocppChargingIntervalData);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Date nextIntervalTime(Date data, String stnRefNum) {
		try {
			String sql = "select datepart(minute, '" + utils.stringToDate(data) + "') as current_minu,datepart(HOUR, '"
					+ utils.stringToDate(data) + "') as current_hr,datepart(SECOND, '" + utils.stringToDate(data)
					+ "') as current_sec";
			Map<String, Object> map = executeRepository.findAll(sql).get(0);
			double meterMins = Double.valueOf(String.valueOf(map.get("current_minu")));
			double meterHrs = Double.valueOf(String.valueOf(map.get("current_hr")));
			double meterSec = Double.valueOf(String.valueOf(map.get("current_sec")));
			Date meterDate = utils.getDateFrmt(data);

			if ((meterMins >= 0.00 || meterMins >= 60.00) && meterMins < 15.00) {
				meterMins = 15.00;
			} else if (meterMins >= 15.00 && meterMins < 30.00) {
				meterMins = 30.00;
			} else if (meterMins >= 30.00 && meterMins < 45.00) {
				meterMins = 45.00;
			} else if (meterMins >= 45.00 && (meterMins >= 0.00 || meterMins <= 60.00)) {
				meterMins = 60.00;
				// meterHrs = meterHrs + 1;
			} /*
				 * else if(meterSec > 0) { meterMins = 60.00; meterHrs = meterHrs + 1; }
				 */
			sql = "SELECT DATEADD(mi, " + meterMins + ",DATEADD(hour, " + meterHrs + ",'"
					+ utils.stringToDate(meterDate) + "')) as ElapsedTime";
			Map<String, Object> map2 = executeRepository.findAll(sql).get(0);
			data = utils.stringToDate(String.valueOf(map2.get("ElapsedTime")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public Date startIntervalTime(Date data, String stnRefNum) {
		try {
			String sql = "select datepart(minute, '" + utils.stringToDate(data) + "') as current_minu,datepart(HOUR, '"
					+ utils.stringToDate(data) + "') as current_hr,datepart(SECOND, '" + utils.stringToDate(data)
					+ "') as current_sec";
			Map<String, Object> map = executeRepository.findAll(sql).get(0);
			double meterMins = Double.valueOf(String.valueOf(map.get("current_minu")));
			double meterHrs = Double.valueOf(String.valueOf(map.get("current_hr")));
			double meterSec = Double.valueOf(String.valueOf(map.get("current_sec")));
			Date meterDate = utils.getDateFrmt(data);

			if ((meterMins >= 0.00 || meterMins >= 60.00) && meterMins < 15.00) {
				meterMins = 0.00;
			} else if (meterMins >= 15.00 && meterMins < 30.00) {
				meterMins = 15.00;
			} else if (meterMins >= 30.00 && meterMins < 45.00) {
				meterMins = 30.00;
			} else if (meterMins >= 45.00 && (meterMins >= 0.00 || meterMins <= 60.00)) {
				meterMins = 45.00;
				// meterHrs = meterHrs + 1;
			} /*
				 * else if(meterSec > 0) { meterMins = 60.00; meterHrs = meterHrs + 1; }
				 */
			sql = "SELECT DATEADD(mi, " + meterMins + ",DATEADD(hour, " + meterHrs + ",'"
					+ utils.stringToDate(meterDate) + "')) as ElapsedTime";
			Map<String, Object> map2 = executeRepository.findAll(sql).get(0);
			data = utils.stringToDate(String.valueOf(map2.get("ElapsedTime")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}

	public Date endIntervalTime(Date data, String stnRefNum) {
		try {
			String sql = "select datepart(minute, '" + utils.stringToDate(data) + "') as current_minu,datepart(HOUR, '"
					+ utils.stringToDate(data) + "') as current_hr,datepart(SECOND, '" + utils.stringToDate(data)
					+ "') as current_sec";
			Map<String, Object> map = executeRepository.findAll(sql).get(0);
			double meterMins = Double.valueOf(String.valueOf(map.get("current_minu")));
			double meterHrs = Double.valueOf(String.valueOf(map.get("current_hr")));
			double meterSec = Double.valueOf(String.valueOf(map.get("current_sec")));
			Date meterDate = utils.getDateFrmt(data);

			if (meterMins < 15.00) {
				meterMins = 14.00;
			} else if (meterMins >= 15.00 && meterMins < 30.00) {
				meterMins = 29.00;
			} else if (meterMins >= 30.00 && meterMins < 45.00) {
				meterMins = 44.00;
			} else if (meterMins >= 45.00 && (meterMins >= 0.00 || meterMins <= 60.00)) {
				meterMins = 59.00;
				// meterHrs = meterHrs + 1;
			} /*
				 * else if(meterSec > 0) { meterMins = 60.00; meterHrs = meterHrs + 1; }
				 */
			sql = "SELECT DATEADD(SECOND, 59,DATEADD(mi, " + meterMins + ",DATEADD(hour, " + meterHrs + ",'"
					+ utils.stringToDate(meterDate) + "'))) as ElapsedTime";
			Map<String, Object> map2 = executeRepository.findAll(sql).get(0);
			data = utils.stringToDate(String.valueOf(map2.get("ElapsedTime")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
}
