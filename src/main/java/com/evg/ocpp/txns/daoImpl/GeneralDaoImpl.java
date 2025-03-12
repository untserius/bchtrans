package com.evg.ocpp.txns.daoImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.evg.ocpp.txns.dao.GeneralDao;
import com.evg.ocpp.txns.model.ocpp.BaseEntity;

@Repository
@Component
public class GeneralDaoImpl<I> implements GeneralDao<BaseEntity, I> {

	@Autowired
	protected SessionFactory sessionFactory;

	private final static Logger logger = LoggerFactory.getLogger(GeneralDaoImpl.class);
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional(readOnly = true)
	public List<Map<String, Object>> getMapData(String hql) {
		List<Map<String, Object>> mapObj = new ArrayList<>();
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
		    transaction = session.getTransaction();
		    transaction.begin();
			mapObj = session.createSQLQuery(hql).setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP).list();
		    transaction.commit(); // Commit the transaction
		} catch (Exception e) {
		    if (transaction != null) {
		        transaction.rollback(); // Rollback the transaction in case of an exception
		    }
		    logger.error("",e);
		} finally {
			if (session != null) {
		        session.close();
		    }
		}
		

		return mapObj;
	}

	@Override
	@Transactional
	public <T> T update(T newsEntry) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.update(newsEntry);
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return newsEntry;
	}

	@Override
	@Transactional
	public <T> T saveOrupdate(T newsEntry) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.saveOrUpdate(newsEntry);
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return newsEntry;
	}

	@Override
	@Transactional
	public <T> T save(T newsEntry) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.save(newsEntry);
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return newsEntry;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public <T> T findById(T newsEntry, long id) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			newsEntry = (T) session.get(newsEntry.getClass(), id);
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		if (newsEntry == null) {
			return null;
		} else {
			return newsEntry;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public <T> T findOne(String hql, T newsEntry){
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			newsEntry = (T) session.createQuery(hql).setMaxResults(1).uniqueResult();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		if (newsEntry == null) {
			return null;
		} else {
			return newsEntry;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public <T> List<T> findAll(String hql, T newsEntry){
		List<T> t = new ArrayList<>();
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			t = (List<T>)session.createQuery(hql).list();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return t;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public <T> T findOneBySQLQuery(String hql, T newsEntry){
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			newsEntry = (T) session.createSQLQuery(hql).addEntity(newsEntry.getClass()).setMaxResults(1).uniqueResult();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		if (newsEntry == null) {
			return null;
		} else {
			return newsEntry;
		}
	}

	@Override
	@Transactional
	public String updateHqlQuiries(String hql) {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			sessionFactory.getCurrentSession().createQuery(hql).executeUpdate();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return "success";
	}

	@Override
	@Transactional
	public String getSingleRecord(String hql) {
		String result = "0";
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			result = String.valueOf(session.createQuery(hql).setMaxResults(1).uniqueResult());
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return result;
	}

	@Override
	@Transactional
	public String getRecordBySql(String hql) {
		String result = "0";
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			result = String.valueOf(session.createSQLQuery(hql).setMaxResults(1).uniqueResult());
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return result;
	}

	@Override
	@Transactional
	public Boolean getFlag(String hql) {
		Boolean result = false;
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			result = (Boolean) session.createQuery(hql).setMaxResults(1).uniqueResult();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		if (result == null) {
			return false;
		}
		return result;
	}

	@Override
	@Transactional
	public Boolean getStationMaxSessionFlag(String hql) {
		Boolean stationMaxSesFlag = false;
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			stationMaxSesFlag = (Boolean) session.createQuery(hql).setMaxResults(1).uniqueResult();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return stationMaxSesFlag == null ? false : stationMaxSesFlag;
	}

	@Override
	@Transactional
	public <T> List<T> findAllSQLQuery(T newsEntry, String query)  {
		List<T> list = new ArrayList<>();
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			list = session.createSQLQuery(query).addEntity(newsEntry.getClass()).list();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return list;
	}
	
	@Override
	@Transactional
	public List findSQLQuery(String query)  {
		List list = new ArrayList<>();
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			list = session.createSQLQuery(query).list();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return list;
	}

	@SuppressWarnings({ "unchecked", })
	@Override
	@Transactional
	public <T> List<T> findAllHQLQuery(T newsEntry, String query)  {
		List<T> list = new ArrayList<>();
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			list = session.createQuery(query).list();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return list;
	}

	@Override
	@Transactional
	public <T> T findOneHQLQuery(T newsEntry, String query)  {
		Session session = sessionFactory.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			newsEntry = (T) session.createQuery(query).uniqueResult();
			transaction.commit();
		}catch (Exception e) {
			if (transaction != null) {
		        transaction.rollback();
		    }
		    logger.error("",e);
		}finally {
			if (session != null) {
		        session.close();
		    }
		}
		return newsEntry;
	}
}
