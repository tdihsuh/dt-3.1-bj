package com.hansight.kunlun.web.base;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.LockMode;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class BaseDao<T> {	
	@Autowired
	protected HibernateTemplate hibernateTemplate;

	public Session openSession() {
		return hibernateTemplate.getSessionFactory().openSession();
	}

	public Serializable save(Object entity) throws HibernateException {
		return hibernateTemplate.save(entity);
	}

	public Serializable save(String entityName, Object entity)
			throws HibernateException {
		return hibernateTemplate.save(entityName, entity);
	}

	public void update(Object entity) throws HibernateException {
		hibernateTemplate.update(entity);
	}

	public void update(Object entity, LockMode lockMode)
			throws HibernateException {
		hibernateTemplate.update(entity, lockMode);
	}

	public void update(String entityName, Object entity)
			throws HibernateException {
		hibernateTemplate.update(entityName, entity);
	}

	public void update(String entityName, Object entity, LockMode lockMode)
			throws HibernateException {
		hibernateTemplate.update(entityName, entity, lockMode);
	}

	public void saveOrUpdate(Object entity) throws HibernateException {
		hibernateTemplate.saveOrUpdate(entity);
	}

	public void saveOrUpdate(String entityName, Object entity)
			throws HibernateException {
		hibernateTemplate.saveOrUpdate(entityName, entity);
	}

	public void saveOrUpdateAll(Collection<T> entities) throws HibernateException {
		hibernateTemplate.saveOrUpdateAll(entities);
	}

	public void delete(Object entity) throws HibernateException {
		hibernateTemplate.delete(entity);
	}

	public void delete(Object entity, LockMode lockMode)
			throws HibernateException {
		hibernateTemplate.delete(entity);
	}

	public void deleteAll(Collection<T> entities) throws HibernateException {
		hibernateTemplate.deleteAll(entities);
	}

	public Object load(Class<T> entityClass, Serializable id)
			throws HibernateException {
		return hibernateTemplate.load(entityClass, id);
	}

	public Object load(String entityName, Serializable id)
			throws HibernateException {
		return hibernateTemplate.load(entityName, id);
	}

	public Object load(Class<T> entityClass, Serializable id, LockMode lockMode)
			throws HibernateException {
		return hibernateTemplate.load(entityClass, id, lockMode);
	}

	public Object load(String entityName, Serializable id, LockMode lockMode)
			throws HibernateException {
		return hibernateTemplate.load(entityName, id, lockMode);
	}

	public List<T> loadAll(Class<T> entityClass) throws HibernateException {
		return hibernateTemplate.loadAll(entityClass);
	}

	public List<T> find(String queryString) throws HibernateException {
		return hibernateTemplate.find(queryString);
	}

	public List<T> find(String queryString, Object value)
			throws HibernateException {
		return hibernateTemplate.find(queryString, value);
	}

	public List<T> find(String queryString, Object values[])
			throws HibernateException {
		return hibernateTemplate.find(queryString, values);
	}

	public List<T> findByExample(Object exampleEntity) throws HibernateException {
		return hibernateTemplate.findByExample(exampleEntity);
	}

	public List<T> findByExample(Object exampleEntity, int firstResult,
			int maxResults) throws HibernateException {
		return hibernateTemplate.findByExample(exampleEntity, firstResult,
				maxResults);
	}

	public List<T> findByNamedParam(String queryString, String paramName,
			Object value) throws HibernateException {
		return hibernateTemplate
				.findByNamedParam(queryString, paramName, value);
	}

	public List<T> find(String queryString, String paramNames[], Object values[])
			throws HibernateException {
		return hibernateTemplate.findByNamedParam(queryString, paramNames,
				values);
	}

	public List<T> findByNamedQuery(String queryName) throws HibernateException {
		return hibernateTemplate.findByNamedQuery(queryName);
	}

	public List<T> findByNamedQuery(String queryName, Object value)
			throws HibernateException {
		return hibernateTemplate.findByNamedQuery(queryName, value);
	}

	public List<T> findByNamedQuery(String queryName, Object values[])
			throws HibernateException {
		return hibernateTemplate.findByNamedQuery(queryName, values);
	}

	public List<T> findByNamedQueryAndNamedParam(String queryName,
			String paramName, Object value) throws HibernateException {
		return hibernateTemplate.findByNamedQueryAndNamedParam(queryName,
				paramName, value);
	}

	public List<T> findByNamedQueryAndNamedParam(String queryName,
			String paramNames[], Object values[]) throws HibernateException {
		return hibernateTemplate.findByNamedQueryAndNamedParam(queryName,
				paramNames, values);
	}

	public List<T> findByNamedQueryAndValueBean(String queryName, Object valueBean)
			throws HibernateException {
		return hibernateTemplate.findByNamedQueryAndValueBean(queryName,
				valueBean);
	}

	public List<T> findByValueBean(String queryString, Object valueBean)
			throws HibernateException {
		return hibernateTemplate.findByValueBean(queryString, valueBean);
	}

	public List<T> findByCriteria(DetachedCriteria criteria)
			throws HibernateException {
		return hibernateTemplate.findByCriteria(criteria);
	}

	public List<T> findByCriteria(DetachedCriteria criteria, int firstResult,
			int maxResults) throws HibernateException {
		return hibernateTemplate.findByCriteria(criteria, firstResult,
				maxResults);
	}

	public int bulkUpdate(String queryString) throws HibernateException {
		return hibernateTemplate.bulkUpdate(queryString);
	}

	public int bulkUpdate(String queryString, Object value)
			throws HibernateException {
		return hibernateTemplate.bulkUpdate(queryString, value);
	}

	public int bulkUpdate(String queryString, Object values[])
			throws HibernateException {
		return hibernateTemplate.bulkUpdate(queryString, values);
	}

	public void clear() throws HibernateException {
		hibernateTemplate.clear();
	}

	public void closeIterator(Iterator<T> iter) throws HibernateException {
		hibernateTemplate.closeIterator(iter);
	}

	public boolean contains(Object entity) throws HibernateException {
		return hibernateTemplate.contains(entity);
	}

	public DataAccessException convertHibernateAccessException(
			HibernateException ex) throws HibernateException {
		return hibernateTemplate.convertHibernateAccessException(ex);
	}

	public void evict(Object entity) throws HibernateException {
		hibernateTemplate.evict(entity);
	}

	public Filter enableFilder(String filterName) throws HibernateException {
		return hibernateTemplate.enableFilter(filterName);
	}

	public void initialize(Object entity) throws HibernateException {
		hibernateTemplate.initialize(entity);
	}

	public Object get(Class<T> entityClass, Serializable id)
			throws HibernateException {
		return hibernateTemplate.get(entityClass, id);
	}

	public Object get(String entityName, Serializable id)
			throws HibernateException {
		return hibernateTemplate.get(entityName, id);
	}

	public Object get(Class<T> entityClass, Serializable id, LockMode lockMode)
			throws HibernateException {
		return hibernateTemplate.get(entityClass, id, lockMode);
	}

	public Object get(String entityName, Serializable id, LockMode lockMode)
			throws HibernateException {
		return hibernateTemplate.get(entityName, id, lockMode);
	}

	public Interceptor getEntityInterceptor() throws HibernateException {
		return hibernateTemplate.getEntityInterceptor();
	}

	public int getFetchSize() throws HibernateException {
		return hibernateTemplate.getFetchSize();
	}

	public String[] getFilterNames() throws HibernateException {
		return hibernateTemplate.getFilterNames();
	}

	public int getFlushMode() throws HibernateException {
		return hibernateTemplate.getFlushMode();
	}

	public SQLExceptionTranslator getJdbcExceptionTranslator()
			throws HibernateException {
		return hibernateTemplate.getJdbcExceptionTranslator();
	}

	public int getMaxResults() throws HibernateException {
		return hibernateTemplate.getMaxResults();
	}

	public String getQueryCacheRegion() throws HibernateException {
		return hibernateTemplate.getQueryCacheRegion();
	}

	public SessionFactory getSessionFactory() throws HibernateException {
		return hibernateTemplate.getSessionFactory();
	}

	public boolean isAllowCreate() throws HibernateException {
		return hibernateTemplate.isAllowCreate();
	}

	public boolean isAlwaysUseNewSession() throws HibernateException {
		return hibernateTemplate.isAlwaysUseNewSession();
	}

	public boolean isCacheQueries() throws HibernateException {
		return hibernateTemplate.isCacheQueries();
	}

	public boolean isCheckWriteOperations() throws HibernateException {
		return hibernateTemplate.isCheckWriteOperations();
	}

	public boolean isExposeNativeSession() throws HibernateException {
		return hibernateTemplate.isExposeNativeSession();
	}

	public void persist(Object entity) throws HibernateException {
		hibernateTemplate.persist(entity);
	}

	public void persist(String entityName, Object entity)
			throws HibernateException {
		hibernateTemplate.persist(entityName, entity);
	}

	public void merge(Object entity) throws HibernateException {
		hibernateTemplate.merge(entity);
	}

	public void merge(String entityName, Object entity)
			throws HibernateException {
		hibernateTemplate.merge(entityName, entity);
	}

	public void refresh(Object entity) throws HibernateException {
		hibernateTemplate.refresh(entity);
	}

	public void refresh(Object entity, LockMode lockMode)
			throws HibernateException {
		hibernateTemplate.refresh(entity, lockMode);
	}

	public void replicate(Object entity, ReplicationMode replicationMode)
			throws HibernateException {
		hibernateTemplate.replicate(entity, replicationMode);
	}

	public void replicate(String entityName, Object entity,
			ReplicationMode replicationMode) throws HibernateException {
		hibernateTemplate.replicate(entityName, entity, replicationMode);
	}

	public void setAllowCreate(boolean allowCreate) throws HibernateException {
		hibernateTemplate.setAllowCreate(allowCreate);
	}

	public void setBeanFactory(BeanFactory beanFactory)
			throws HibernateException {
		hibernateTemplate.setBeanFactory(beanFactory);
	}

	public void setCacheQueries(boolean cacheQueries) throws HibernateException {
		hibernateTemplate.setCacheQueries(cacheQueries);
	}

	public void setCheckWriteOperations(boolean checkWriteOperations)
			throws HibernateException {
		hibernateTemplate.setCheckWriteOperations(checkWriteOperations);
	}

	public void setEntityInterceptor(Interceptor entityInterceptor)
			throws HibernateException {
		hibernateTemplate.setEntityInterceptor(entityInterceptor);
	}

	public void setEntityInterceptorBeanName(String entityInterceptorBeanName)
			throws HibernateException {
		hibernateTemplate
				.setEntityInterceptorBeanName(entityInterceptorBeanName);
	}

	public void setExposeNativeSession(boolean exposeNativeSession)
			throws HibernateException {
		hibernateTemplate.setExposeNativeSession(exposeNativeSession);
	}

	public void setFetchSize(int fetchSize) throws HibernateException {
		hibernateTemplate.setFetchSize(fetchSize);
	}

	public void setFilterName(String filter) throws HibernateException {
		hibernateTemplate.setFilterName(filter);
	}

	public void setFilterNames(String filterNames[]) throws HibernateException {
		hibernateTemplate.setFilterNames(filterNames);
	}

	public void setFlushMode(int flushMode) throws HibernateException {
		hibernateTemplate.setFlushMode(flushMode);
	}

	public void setFlushModeName(String constantName) throws HibernateException {
		hibernateTemplate.setFlushModeName(constantName);
	}

	public void setJdbcExceptionTranslator(
			SQLExceptionTranslator jdbcExceptionTranslator)
			throws HibernateException {
		hibernateTemplate.setJdbcExceptionTranslator(jdbcExceptionTranslator);
	}

	public void setMaxResults(int maxResults) throws HibernateException {
		hibernateTemplate.setMaxResults(maxResults);
	}

	public void setQueryCacheRegion(String queryCacheRegion)
			throws HibernateException {
		hibernateTemplate.setQueryCacheRegion(queryCacheRegion);
	}

	public void setSessionFactory(SessionFactory sessionFactory)
			throws HibernateException {
		hibernateTemplate.setSessionFactory(sessionFactory);
	}
	
	public List<T> query(String queryString){
		return find(queryString);
	}
	
	public List<T> query(DetachedCriteria criteria){
		return findByCriteria(criteria);
	}
	
	public List<Object> queryObject(String query){
		return (List<Object>) find(query);
	}
	
	
	
}
