package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.service.vo.TRoleQueryBean;

@Repository
public class TRoleDao extends BaseDao<TRole>{
	
	public Serializable addTRole(TRole tRole)throws Exception{
		return save(tRole);
	}
	
	public void updateTRole(TRole tRole)throws Exception{
		update(tRole);
	}
	
	public void deleteTRole(TRole tRole)throws Exception{
		delete(tRole);
	}
	
	public TRole getTRoleById(String id)throws Exception{
		return (TRole)get(TRole.class, id);
	}

	public List<TRole> queryTRole(TRoleQueryBean tRoleQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRole.class, "t");
		if(tRoleQueryBean != null){
			if(tRoleQueryBean.getUseFlag() != null && tRoleQueryBean.getUseFlag().longValue() != 0)
				criteria.add(Restrictions.eq("useFlag", tRoleQueryBean.getUseFlag()));
			if(tRoleQueryBean.getSortid() != null && tRoleQueryBean.getSortid().longValue() != 0)
				criteria.add(Restrictions.eq("sortid", tRoleQueryBean.getSortid()));
			if(tRoleQueryBean.getName() != null && !"".equals(tRoleQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", tRoleQueryBean.getName()));
			if(tRoleQueryBean.getId() != null && !"".equals(tRoleQueryBean.getId().trim()))
				criteria.add(Restrictions.eq("id", tRoleQueryBean.getId()));
			if(tRoleQueryBean.getDescription() != null && !"".equals(tRoleQueryBean.getDescription().trim()))
				criteria.add(Restrictions.like("description", tRoleQueryBean.getDescription()));
		}
		return query(criteria);
	}
	
	public List<TRole> queryTRoleByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRole.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TRole> queryTRoleByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRole.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
}