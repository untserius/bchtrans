package com.evg.ocpp.txns.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evg.ocpp.txns.Service.preferedNotifyService;
import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.PreferredNotification;

@Service
public class preferedNotifyServiceImpl implements preferedNotifyService{
	
	@Autowired
	private GeneralDao<?, ?> generalDao;
	
	@Override
	public PreferredNotification getPreferedNofy(long userId) {
		PreferredNotification pn = null;
		try {
			pn = generalDao.findOne("FROM PreferredNotification WHERE userId="+userId, new PreferredNotification());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return pn;
	}
}
