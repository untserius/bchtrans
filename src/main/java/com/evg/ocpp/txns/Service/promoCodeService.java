package com.evg.ocpp.txns.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface promoCodeService {

	boolean promoCodeValidating(String promoCode, long userId, long protId);

	void updateSessionInPromoHistory(long promoHisId, String sessionId);

	Map<String, Object> getPromoCodeBilling(String promoCode);

	Map<String, Object> getRewardDataByUserId(long userId);

	void updateAmtRewardBalance(BigDecimal usedRewards, BigDecimal rewardBalance, long userId, String stnRefNum);

	void updatekWhRewardBalance(BigDecimal usedRewards, BigDecimal rewardBalance, long userId, String stnRefNum);

}
