package com.evg.ocpp.txns.ServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.promoCodeService;
import com.evg.ocpp.txns.repository.ExecuteRepository;
import com.evg.ocpp.txns.utils.LoggerUtil;
import com.evg.ocpp.txns.utils.Utils;

@Service
public class promoCodeServiceImpl implements promoCodeService{
	private final static Logger logger = LoggerFactory.getLogger(promoCodeServiceImpl.class);
	
	@Autowired
	private ExecuteRepository executeRepositoy;
	
	@Autowired
	private LoggerUtil customLogger;
	
	@Autowired
	private Utils utils;

	@Override
	public boolean promoCodeValidating(String promoCode,long userId,long protId) {
		boolean flag = false;
		try {
			String str = "select top 1 id,flag from promoCodeHistory where sessionId is null and promoCode = '"+promoCode+"' and and userId="+userId+" order by id desc";
			List<Map<String, Object>> findAll = executeRepositoy.findAll(str);
			if(findAll.size() > 0 && String.valueOf(findAll.get(0).get("flag")).equalsIgnoreCase("1")) {
				flag = true;
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return flag;
	}
	
	@Override
	public void updateSessionInPromoHistory(long promoHisId,String sessionId) {
		try {
			String str = "update promoCodeHistory set flag=0,sessionId='"+sessionId+"' where id="+promoHisId;
			executeRepositoy.update(str);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	
	@Override
	public Map<String,Object> getRewardDataByUserId(long userId) {
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select amount,kWh from promocode_reward where userId="+userId;
			List<Map<String, Object>> findAll = executeRepositoy.findAll(str);
			if(findAll.size() > 0) {
				map = findAll.get(0);
			}else {
				map.put("amount", "0");
				map.put("kWh", "0");
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}
	
	public Map<String,Object> getPromoCodeDataBySessionId(String sessionId) {
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select amount,amountType from promoCodeHistory where sessionId='"+sessionId+"'";
			List<Map<String, Object>> findAll = executeRepositoy.findAll(str);
			if(findAll.size() > 0) {
				map = findAll.get(0);
			}else {
				map.put("amountType", "");
				map.put("amount", "0");
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}

	@Override
	public Map<String,Object> getPromoCodeBilling(String promoCode) {
		Map<String,Object> map = new HashMap<>();
		try {
			String str = "select pc.pname,pc.amountType,pc.amount,'true' as promoCodeFlag from promoCode_in_codes pic inner join promoCode pc on pic.promoId = pc.id where pic.promoCode = '"+promoCode+"'";
			List<Map<String, Object>> findAll = executeRepositoy.findAll(str);
			if(findAll.size() > 0) {
				map = findAll.get(0);
			}else {
				map.put("pname", "");
				map.put("amount", "0");
				map.put("amountType", "");
				map.put("promoCodeFlag", "false");
			}
		}catch (Exception e) {
			logger.error("",e);
		}
		return map;
	}
	@Override
	public void updateAmtRewardBalance(BigDecimal usedRewards,BigDecimal rewardBalance,long userId,String stnRefNum) {
		try {
			BigDecimal remainingAmt = rewardBalance.subtract(usedRewards).setScale(2, BigDecimal.ROUND_UP);
			String update = "update promoCode_reward set amount='"+remainingAmt+"' where userId="+userId;
			executeRepositoy.update(update);
			customLogger.info(stnRefNum, "update promoCode balance amt query : "+update);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
	@Override
	public void updatekWhRewardBalance(BigDecimal usedRewards,BigDecimal rewardBalance,long userId,String stnRefNum) {
		try {
			BigDecimal remainingkWh = rewardBalance.subtract(usedRewards).setScale(2, BigDecimal.ROUND_UP);
			String update = "update promoCode_reward set kWh='"+remainingkWh+"' where userId="+userId;
			executeRepositoy.update(update);
			customLogger.info(stnRefNum, "update promoCode balance kWh query : "+update);
		}catch (Exception e) {
			logger.error("",e);
		}
	}
}
