package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TAuthorityRequestmap;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityRequestmapQueryBean;

@Repository
public class TAuthorityRequestmapDao extends BaseDao<TAuthorityRequestmap>{
	
	public Serializable addTAuthorityRequestmap(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		return save(tAuthorityRequestmap);
	}
	
	public void updateTAuthorityRequestmap(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		update(tAuthorityRequestmap);
	}
	
	public void deleteTAuthorityRequestmap(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		delete(tAuthorityRequestmap);
	}
	
	public TAuthorityRequestmap getTAuthorityRequestmapById(String id)throws Exception{
		return (TAuthorityRequestmap)get(TAuthorityRequestmap.class, id);
	}

	public List<TAuthorityRequestmap> queryTAuthorityRequestmap(TAuthorityRequestmapQueryBean tAuthorityRequestmapQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthorityRequestmap.class, "t");
		if(tAuthorityRequestmapQueryBean != null){
			if(tAuthorityRequestmapQueryBean.getAuthorityId() != null && !"".equals(tAuthorityRequestmapQueryBean.getAuthorityId().trim()))
				criteria.add(Restrictions.like("authorityId", tAuthorityRequestmapQueryBean.getAuthorityId()));
			if(tAuthorityRequestmapQueryBean.getRequestmapId() != null && !"".equals(tAuthorityRequestmapQueryBean.getRequestmapId().trim()))
				criteria.add(Restrictions.like("requestmapId", tAuthorityRequestmapQueryBean.getRequestmapId()));
			if(tAuthorityRequestmapQueryBean.getId() != null && !"".equals(tAuthorityRequestmapQueryBean.getId().trim()))
				criteria.add(Restrictions.eq("id", tAuthorityRequestmapQueryBean.getId()));
		}
		return query(criteria);
	}
	
	public List<TAuthorityRequestmap> queryTAuthorityRequestmapByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthorityRequestmap.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TAuthorityRequestmap> queryTAuthorityRequestmapByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthorityRequestmap.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public void delAuthority(String authorityId,String[] requestmapIds) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthorityRequestmap.class, "t");
		criteria.add(Restrictions.eq("authorityId", authorityId));
		criteria.add(Restrictions.in("requestmapId", requestmapIds));
		
		deleteAll(query(criteria));
	}
	
}