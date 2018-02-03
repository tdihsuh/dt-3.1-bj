package com.hansight.kunlun.web.config.datasource.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfForwarderQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_FORWARDER表对应的DAO
 */
@Repository
public class ConfForwarderDao extends BaseDao<ConfForwarder>{
	public Serializable addConfForwarder(ConfForwarder confForwarder)throws Exception{
		return save(confForwarder);
	}
	
	public void updateConfForwarder(ConfForwarder confForwarder)throws Exception{
		update(confForwarder);
	}
	
	public void deleteConfForwarder(ConfForwarder confForwarder)throws Exception{
		delete(confForwarder);
	}
	
	public ConfForwarder getConfForwarderById(String id)throws Exception{
		return (ConfForwarder)get(ConfForwarder.class, id);
	}
	public List<ConfForwarder> queryConfForwarder(ConfForwarderQueryBean confForwarderQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfForwarder.class, "t");
		if(confForwarderQueryBean != null){
			if(confForwarderQueryBean.getName() != null && !"".equals(confForwarderQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", confForwarderQueryBean.getName()));
			if(confForwarderQueryBean.getId() != null && !"".equals(confForwarderQueryBean.getId().trim()))
				criteria.add(Restrictions.like("id", confForwarderQueryBean.getId()));
			if(confForwarderQueryBean.getDescription() != null && !"".equals(confForwarderQueryBean.getDescription().trim()))
				criteria.add(Restrictions.like("description", confForwarderQueryBean.getDescription()));
			if(confForwarderQueryBean.getIp() != null && !"".equals(confForwarderQueryBean.getIp().trim()))
				criteria.add(Restrictions.like("ip", confForwarderQueryBean.getIp()));
			if(confForwarderQueryBean.getState() != null && !"".equals(confForwarderQueryBean.getState().trim()))
				criteria.add(Restrictions.like("state", confForwarderQueryBean.getState()));
			if(confForwarderQueryBean.getCreateDate() != null && !"".equals(confForwarderQueryBean.getCreateDate()))
				criteria.add(Restrictions.like("createDate", confForwarderQueryBean.getCreateDate()));
		}
		return query(criteria);
	}
	public List<ConfForwarder> queryConfForwarderByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfForwarder.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<ConfForwarder> queryConfForwarderByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfForwarder.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	public boolean forwarderNameValid(String name)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfForwarder WHERE name ='").append(name).append("'");
		return query(hql.toString()).size()>0?true:false;
	}
	public Pagination queryPagination(String Hql,int currentPage,int pageSize)throws Exception{
		Query query = hibernateTemplate.getSessionFactory().getCurrentSession().createQuery(Hql);
		query.setFirstResult((currentPage -1) * pageSize);
		query.setMaxResults(pageSize);
		List datas = query.list();
		Pagination pm = new Pagination();
		pm.setList(datas);
		pm.setTotalCount(datas.size());
		return pm;
	}
	public int agentTotal(int pageSize)throws Exception{
		StringBuffer hql = new StringBuffer();
		int totalCount  = 0;
		hql.append("SELECT COUNT(*) FROM ConfForwarder");
		List<Object> listObj =queryObject(hql.toString());
		if(listObj.size()!=0){
			int total = Integer.parseInt(listObj.get(0).toString());
			totalCount = total % pageSize == 0?(total / pageSize):((total/ pageSize) + 1);
		}
		return totalCount;
	}
}
