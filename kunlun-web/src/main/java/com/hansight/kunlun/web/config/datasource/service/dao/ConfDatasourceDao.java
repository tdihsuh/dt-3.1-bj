package com.hansight.kunlun.web.config.datasource.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;








import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDatasourceQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DATASOURCE表对应的DAO
 */
@Repository
public class ConfDatasourceDao extends BaseDao<ConfDatasource>{
	

	public Serializable addConfDatasource(ConfDatasource dataSource)throws Exception{
		return save(dataSource);
	}
	
	public void updateConfDatasource(ConfDatasource dataSource)throws Exception{
		update(dataSource);
	}
	
	public void deleteConfDatasource(ConfDatasource dataSource)throws Exception{
		delete(dataSource);
	}
	
	public ConfDatasource getConfDatasourceById(String id)throws Exception{
		return (ConfDatasource)get(ConfDatasource.class, id);
	}

	public List<ConfDatasource> queryConfDatasource(ConfDatasourceQueryBean confDatasourceQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDatasource.class);
		if(confDatasourceQueryBean != null){
			if(confDatasourceQueryBean.getId() != null)
				criteria.add(Restrictions.eq("id", confDatasourceQueryBean.getId()));
			if(confDatasourceQueryBean.getAgentId() != null)
				criteria.add(Restrictions.eq("agentId", confDatasourceQueryBean.getAgentId()));
			if(confDatasourceQueryBean.getCategoryId() != null)
				criteria.add(Restrictions.eq("categoryId", confDatasourceQueryBean.getCategoryId()));
			if(confDatasourceQueryBean.getEncoding() != null && !"".equals(confDatasourceQueryBean.getEncoding().trim()))
				criteria.add(Restrictions.eq("encoding", confDatasourceQueryBean.getEncoding()));
			if(confDatasourceQueryBean.getHost() != null && !"".equals(confDatasourceQueryBean.getHost().trim()))
				criteria.add(Restrictions.like("host", confDatasourceQueryBean.getHost()));
			if(confDatasourceQueryBean.getAgentParser() != null && !"".equals(confDatasourceQueryBean.getAgentParser().trim()))
				criteria.add(Restrictions.eq("agentParser", confDatasourceQueryBean.getAgentParser()));
			if(confDatasourceQueryBean.getPattern() != null && !"".equals(confDatasourceQueryBean.getPattern().trim()))
				criteria.add(Restrictions.eq("pattern", confDatasourceQueryBean.getPattern()));
			if(confDatasourceQueryBean.getPort() != null && !"".equals(confDatasourceQueryBean.getPort().toString().trim()))
				criteria.add(Restrictions.eq("port", confDatasourceQueryBean.getPort()));
			if(confDatasourceQueryBean.getProtocol() != null && !"".equals(confDatasourceQueryBean.getProtocol().trim()))
				criteria.add(Restrictions.eq("protocol", confDatasourceQueryBean.getProtocol()));
			if(confDatasourceQueryBean.getType() != null && !"".equals(confDatasourceQueryBean.getType().trim()))
				criteria.add(Restrictions.eq("type", confDatasourceQueryBean.getType()));
			if(confDatasourceQueryBean.getUrl() != null && !"".equals(confDatasourceQueryBean.getUrl().trim()))
				criteria.add(Restrictions.eq("url", confDatasourceQueryBean.getUrl()));
			if(confDatasourceQueryBean.getCategory() != null && !"".equals(confDatasourceQueryBean.getCategory().trim()))
				criteria.add(Restrictions.eq("category", confDatasourceQueryBean.getCategory()));
			if(confDatasourceQueryBean.getForwarderParser() != null && !"".equals(confDatasourceQueryBean.getForwarderParser().trim()))
				criteria.add(Restrictions.eq("forwarderParser", confDatasourceQueryBean.getForwarderParser()));
			if(confDatasourceQueryBean.getState() != null && !"".equals(confDatasourceQueryBean.getState().trim()))
				criteria.add(Restrictions.eq("state", confDatasourceQueryBean.getState()));
			if(confDatasourceQueryBean.getForwarderName() != null && !"".equals(confDatasourceQueryBean.getForwarderName().trim()))
				criteria.add(Restrictions.eq("forwarderName", confDatasourceQueryBean.getForwarderName()));
			if(confDatasourceQueryBean.getDatasourceName() != null && !"".equals(confDatasourceQueryBean.getDatasourceName().trim()))
				criteria.add(Restrictions.eq("datasourceName", confDatasourceQueryBean.getDatasourceName()));
			if(confDatasourceQueryBean.getConfig() != null && !"".equals(confDatasourceQueryBean.getConfig().trim()))
				criteria.add(Restrictions.eq("config", confDatasourceQueryBean.getConfig()));
			if(confDatasourceQueryBean.getCreateDate() != null && !"".equals(confDatasourceQueryBean.getCreateDate()))
				criteria.add(Restrictions.eq("createDate", confDatasourceQueryBean.getCreateDate()));
			if(confDatasourceQueryBean.getConfigInfo() != null && !"".equals(confDatasourceQueryBean.getConfigInfo().trim()))
				criteria.add(Restrictions.eq("configInfo", confDatasourceQueryBean.getConfigInfo()));
		}
		/**Pagination pm = new Pagination();
		List<ConfDatasource> confDatasource = query(criteria);
		pm.setList(confDatasource);
		pm.setTotalCount(confDatasource.size());**/
		return  query(criteria);
	}
	public List<ConfDatasource> queryConfDatasourceByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDatasource.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	public List<ConfDatasource> queryConfDatasourceByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfDatasource.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	public List<ConfDatasource> queryDatasourceById(String url)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDatasource WHERE url ='").append(url).append("'");
		System.out.println("hql:::"+hql.toString());
		return query(hql.toString());
	}
	public List<ConfDatasource> queryDatasourceById2(String host,String port)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDatasource WHERE host ='").append(host).append("'");
		hql.append(" AND port =").append(port).append("");
		System.out.println("hql:::"+hql.toString());
		return query(hql.toString());
	}
	public List<ConfDatasource>queryDatasourceAgentById(String agentId)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDatasource WHERE agentId ='").append(agentId).append("'");
		return query(hql.toString());
	}
	
	public Pagination findByPages(ConfDatasource confData ,int firstResult,
			int maxResults )throws Exception{
		Pagination pm = new Pagination();
		List<ConfDatasource> confDataSource = findByExample(confData, firstResult, maxResults);
		pm.setList(confDataSource);
		pm.setTotalCount(confDataSource.size());
		return pm;
	}
	
	public List<Object> queryDataSource(String query)throws Exception{
		return queryObject(query);
	}
	public boolean datasourceNameValid(String datasourcename)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDatasource WHERE datasourceName ='").append(datasourcename).append("'");
		return query(hql.toString()).size()>0?true:false;
	}
	public boolean datasourceUrlValid(String url)throws Exception{
		StringBuffer hql = new StringBuffer();
		hql.append("FROM ConfDatasource WHERE url ='").append(url).append("'");
		return query(hql.toString()).size()>0?true:false;
	}
	public Pagination query (String Hql,int offSet,int pageSize )throws Exception{
		Query query = hibernateTemplate.getSessionFactory().getCurrentSession().createQuery(Hql);
		query.setFirstResult((offSet -1) * pageSize);
		query.setMaxResults(pageSize);
		List datas = query.list();
		Pagination pm = new Pagination();
		pm.setList(datas);
		pm.setTotalCount(datas.size());
		return pm;
	}
	public int totalPages (int pageSize)throws Exception{
		StringBuffer hql = new StringBuffer();
		int totalCount  = 0;
		hql.append("SELECT COUNT(*) FROM ConfDatasource");
		List<Object> listObj =queryObject(hql.toString());
		if(listObj.size()!=0){
			int total = Integer.parseInt(listObj.get(0).toString());
			totalCount = total % pageSize == 0?(total / pageSize):((total/ pageSize) + 1);
		}
		return totalCount;
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
	public Session saveDataSource(ConfDatasource dataSource)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		 Transaction tx =session.beginTransaction();
		 session.save(dataSource);
		 tx.commit();
		return session;
	}
	public Session updateDataSource(ConfDatasource dataSource)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		Transaction tx =session.beginTransaction();
		session.update(dataSource);
		tx.commit();
		return session;
	}
	public Session deleteDataSource(ConfDatasource dataSource)throws Exception{
		Session session = hibernateTemplate.getSessionFactory().openSession();
		Transaction tx =session.beginTransaction();
		session.delete(dataSource);
		tx.commit();
		return session;
	}
}