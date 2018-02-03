package com.hansight.kunlun.web.config.datasource.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.datasource.entity.ConfAgent;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfAgentQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_AGENT表对应的DAO
 */
@Repository
public class ConfAgentDao extends BaseDao<ConfAgent>{
	public Serializable addConfAgent(ConfAgent confAgent)throws Exception{
		return save(confAgent);
	}
	
	public void updateConfAgent(ConfAgent confAgent)throws Exception{
		update(confAgent);
	}
	
	public void deleteConfAgent(ConfAgent confAgent)throws Exception{
		delete(confAgent);
	}
	
	public ConfAgent getConfAgentById(String id)throws Exception{
		return (ConfAgent)get(ConfAgent.class, id);
	}
	public List<ConfAgent> queryAgentConf(ConfAgentQueryBean confAgentQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfAgent.class);
		if(confAgentQueryBean != null){
			if(confAgentQueryBean.getId() != null && !"".equals(confAgentQueryBean.getId().trim()))
				criteria.add(Restrictions.like("id", confAgentQueryBean.getId()));
			if(confAgentQueryBean.getName() != null && !"".equals(confAgentQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", confAgentQueryBean.getName()));
			if(confAgentQueryBean.getIp() != null && !"".equals(confAgentQueryBean.getIp().trim()))
				criteria.add(Restrictions.like("ip", confAgentQueryBean.getIp()));
			if(confAgentQueryBean.getDescription() != null && !"".equals(confAgentQueryBean.getDescription().trim()))
				criteria.add(Restrictions.like("description", confAgentQueryBean.getDescription()));
			if(confAgentQueryBean.getState() != null && !"".equals(confAgentQueryBean.getState().trim()))
				criteria.add(Restrictions.like("state", confAgentQueryBean.getState()));
			if(confAgentQueryBean.getCreateDate() != null && !"".equals(confAgentQueryBean.getCreateDate()))
				criteria.add(Restrictions.like("createDate", confAgentQueryBean.getCreateDate()));
		}
		return query(criteria);
	}
	public List<ConfAgent> queryConfAgentByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfAgent.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<ConfAgent> queryConfAgentByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfAgent.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	public boolean agentNameValid(String name)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfAgent WHERE name ='").append(name).append("'");
		return query(hql.toString()).size()>0?true:false;
	}
	public int agentTotal(int pageSize)throws Exception{
		StringBuffer hql = new StringBuffer();
		int totalCount  = 0;
		hql.append("SELECT COUNT(*) FROM ConfAgent");
		List<Object> listObj =queryObject(hql.toString());
		if(listObj.size()!=0){
			int total = Integer.parseInt(listObj.get(0).toString());
			totalCount = total % pageSize == 0?(total / pageSize):((total/ pageSize) + 1);
		}
		return totalCount;
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
}
