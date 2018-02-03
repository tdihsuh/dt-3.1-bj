package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TRequestmap;
import com.hansight.kunlun.web.base.user.service.vo.TRequestmapQueryBean;

@Repository
public class TRequestmapDao extends BaseDao<TRequestmap>{
	
	public Serializable addTRequestmap(TRequestmap tRequestmap)throws Exception{
		return save(tRequestmap);
	}
	
	public void updateTRequestmap(TRequestmap tRequestmap)throws Exception{
		update(tRequestmap);
	}
	
	public void deleteTRequestmap(TRequestmap tRequestmap)throws Exception{
		delete(tRequestmap);
	}
	
	public TRequestmap getTRequestmapById(String id)throws Exception{
		return (TRequestmap)get(TRequestmap.class, id);
	}

	public List<TRequestmap> queryTRequestmap(TRequestmapQueryBean tRequestmapQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRequestmap.class, "t");
		if(tRequestmapQueryBean != null){
			if(tRequestmapQueryBean.getUseFlag() != null && tRequestmapQueryBean.getUseFlag().longValue() != 0)
				criteria.add(Restrictions.eq("useFlag", tRequestmapQueryBean.getUseFlag()));
			if(tRequestmapQueryBean.getUrl() != null && !"".equals(tRequestmapQueryBean.getUrl().trim()))
				criteria.add(Restrictions.like("url", tRequestmapQueryBean.getUrl()));
			if(tRequestmapQueryBean.getId() != null && !"".equals(tRequestmapQueryBean.getId().trim()))
				criteria.add(Restrictions.eq("id", tRequestmapQueryBean.getId()));
			if(tRequestmapQueryBean.getDescription() != null && !"".equals(tRequestmapQueryBean.getDescription().trim()))
				criteria.add(Restrictions.like("description", tRequestmapQueryBean.getDescription()));
		}
		return query(criteria);
	}
	
	public List<TRequestmap> queryTRequestmapByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRequestmap.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TRequestmap> queryTRequestmapByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRequestmap.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TRequestmap> queryTRequestmapByAuthorityId(String authorityId) throws Exception{
		StringBuffer hsql = new StringBuffer();
		hsql.append(" select m from TAuthorityRequestmap t,TRequestmap m");
		hsql.append(" where t.requestmapId = m.id");
		hsql.append(" and t.authorityId = '").append(authorityId).append("'");
		hsql.append(" and m.useFlag = 1");
		
		return query(hsql.toString());
	}
	
}