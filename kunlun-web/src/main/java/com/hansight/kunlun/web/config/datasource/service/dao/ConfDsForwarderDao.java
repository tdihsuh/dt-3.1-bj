package com.hansight.kunlun.web.config.datasource.service.dao;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.datasource.entity.ConfDsForwarder;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDsForwarderQueryBean;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.List;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DS_FORWARDER表对应的DAO
 */
@Repository
public class ConfDsForwarderDao extends BaseDao<ConfDsForwarder>{
	
	public Serializable addConfDsForwarder(ConfDsForwarder confDsForwarder)throws Exception{
		return save(confDsForwarder);
	}
	
	public void updateConfDsForwarder(ConfDsForwarder confDsForwarder)throws Exception{
		update(confDsForwarder);
	}
	public void deleteConfDsForwarder(ConfDsForwarder confDsForwarder)throws Exception{
		delete(confDsForwarder);
	}
	
	public ConfDsForwarder getConfDsForwarderById(String id)throws Exception{
		return (ConfDsForwarder)get(ConfDsForwarder.class, id);
	}

	public List<ConfDsForwarder> queryConfDsForwarder(ConfDsForwarderQueryBean confDsForwarderQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDsForwarder.class, "t");
		if(confDsForwarderQueryBean != null){
			if(confDsForwarderQueryBean.getId() != null && confDsForwarderQueryBean.getId().longValue() != 0)
				criteria.add(Restrictions.eq("id", confDsForwarderQueryBean.getId()));
			if(confDsForwarderQueryBean.getDatasourceId() != null && !"".equals(confDsForwarderQueryBean.getDatasourceId().trim()))
				criteria.add(Restrictions.like("datasourceId", confDsForwarderQueryBean.getDatasourceId()));
			if(confDsForwarderQueryBean.getForwarderId() != null && !"".equals(confDsForwarderQueryBean.getForwarderId().trim()))
				criteria.add(Restrictions.like("forwarderId", confDsForwarderQueryBean.getForwarderId()));
		}
		return query(criteria);
	}
	
	public List<ConfDsForwarder> queryConfDsForwarderByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDsForwarder.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<ConfDsForwarder> queryConfDsForwarderByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDsForwarder.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	public List<ConfDsForwarder> queryConfDsForwarderById(String id)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDsForwarder WHERE datasourceId ='").append(id).append("'");
		return query(hql.toString());
	}
	public List<ConfDsForwarder> queryForwarderById(String forwarderid)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDsForwarder WHERE forwarderId ='").append(forwarderid).append("'");
		return query(hql.toString());
	}
	public void rollBack(Session session)throws Exception{
		session.getTransaction().rollback();
		session.close();
	}
	public Session session()throws Exception{
		Session session = openSession();
		session.beginTransaction();
		return session;
	}
	public Session save(ConfDsForwarder confDsForwarder)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		Transaction tx =session.beginTransaction();
		session.save(confDsForwarder);
		tx.commit();
		return session;
	}
	public Session updateDs(ConfDsForwarder confDsForwarder)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		Transaction tx =session.beginTransaction();
		session.update(confDsForwarder);
		tx.commit();
		return session;
	}
	public Session deleteDs(ConfDsForwarder confDsForwarder)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		Transaction tx =session.beginTransaction();
		session.delete(confDsForwarder);
		tx.commit();
		return session;
	}
}