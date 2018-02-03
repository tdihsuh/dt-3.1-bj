package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TRoleAuthority;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;

@Repository
public class TRoleAuthorityDao extends BaseDao<TRoleAuthority>{
	
	public Serializable addTRoleAuthority(TRoleAuthority tRoleAuthority)throws Exception{
		return save(tRoleAuthority);
	}
	
	public void updateTRoleAuthority(TRoleAuthority tRoleAuthority)throws Exception{
		update(tRoleAuthority);
	}
	
	public void deleteTRoleAuthority(TRoleAuthority tRoleAuthority)throws Exception{
		delete(tRoleAuthority);
	}
	
	public TRoleAuthority getTRoleAuthorityById(String id)throws Exception{
		return (TRoleAuthority)get(TRoleAuthority.class, id);
	}

	public List<TRoleAuthority> queryTRoleAuthority(TRoleAuthorityQueryBean tRoleAuthorityQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRoleAuthority.class, "t");
		if(tRoleAuthorityQueryBean != null){
			if(tRoleAuthorityQueryBean.getRoleId() != null && !"".equals(tRoleAuthorityQueryBean.getRoleId().trim()))
				criteria.add(Restrictions.eq("roleId", tRoleAuthorityQueryBean.getRoleId()));
			if(tRoleAuthorityQueryBean.getAuthorityId() != null && !"".equals(tRoleAuthorityQueryBean.getAuthorityId().trim()))
				criteria.add(Restrictions.eq("authorityId", tRoleAuthorityQueryBean.getAuthorityId()));
		}
		return query(criteria);
	}
	
	public List<TRoleAuthority> queryTRoleAuthorityByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TRoleAuthority.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}

	public List<TRoleAuthority> findTRoleAuthorityByRoleIds(List<String> roleIds) throws Exception{
		// TODO Auto-generated method stub
		if(roleIds == null || roleIds.size() == 0) return null;
		DetachedCriteria criteria= DetachedCriteria.forClass(TRoleAuthority.class, "t");
		criteria.add(Restrictions.in("roleId", roleIds));
		return query(criteria);
	}
	
}