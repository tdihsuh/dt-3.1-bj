package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TAuthority;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityQueryBean;

@Repository
public class TAuthorityDao extends BaseDao<TAuthority>{
	
	public Serializable addTAuthority(TAuthority tAuthority)throws Exception{
		return save(tAuthority);
	}
	
	public void updateTAuthority(TAuthority tAuthority)throws Exception{
		update(tAuthority);
	}
	
	public void deleteTAuthority(TAuthority tAuthority)throws Exception{
		delete(tAuthority);
	}
	
	public TAuthority getTAuthorityById(String id)throws Exception{
		return (TAuthority)get(TAuthority.class, id);
	}

	public List<TAuthority> queryTAuthority(TAuthorityQueryBean tAuthorityQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthority.class, "t");
		if(tAuthorityQueryBean != null){
			if(tAuthorityQueryBean.getUseFlag() != null && tAuthorityQueryBean.getUseFlag().longValue() != 0)
				criteria.add(Restrictions.eq("useFlag", tAuthorityQueryBean.getUseFlag()));
			if(tAuthorityQueryBean.getName() != null && !"".equals(tAuthorityQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", tAuthorityQueryBean.getName()));
			if(tAuthorityQueryBean.getId() != null && !"".equals(tAuthorityQueryBean.getId().trim()))
				criteria.add(Restrictions.eq("id", tAuthorityQueryBean.getId()));
			if(tAuthorityQueryBean.getDescription() != null && !"".equals(tAuthorityQueryBean.getDescription().trim()))
				criteria.add(Restrictions.like("description", tAuthorityQueryBean.getDescription()));
		}
		return query(criteria);
	}
	
	public List<TAuthority> queryTAuthorityByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthority.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TAuthority> queryTAuthorityByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TAuthority.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
}