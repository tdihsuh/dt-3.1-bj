package com.hansight.kunlun.web.base.user.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.base.user.entity.TUser;
import com.hansight.kunlun.web.base.user.service.vo.TUserQueryBean;

@Repository
public class TUserDao extends BaseDao<TUser>{
	
	public Serializable addTUser(TUser tUser)throws Exception{
		return save(tUser);
	}
	
	public void updateTUser(TUser tUser)throws Exception{
		update(tUser);
	}
	
	public void deleteTUser(TUser tUser)throws Exception{
		delete(tUser);
	}
	
	public TUser getTUserById(String id)throws Exception{
		return (TUser)get(TUser.class, id);
	}

	public List<TUser> queryTUser(TUserQueryBean tUserQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TUser.class, "t");
		if(tUserQueryBean != null){
			if(tUserQueryBean.getUseFlag() != null && tUserQueryBean.getUseFlag().longValue() != 0)
				criteria.add(Restrictions.eq("useFlag", tUserQueryBean.getUseFlag()));
			if(tUserQueryBean.getCreateDate() != null && !"".equals(tUserQueryBean.getCreateDate())){
				try{
					criteria.add(Restrictions.eq("createDate", tUserQueryBean.getCreateDate()));
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
			if(tUserQueryBean.getUserId() != null && !"".equals(tUserQueryBean.getUserId().trim()))
				criteria.add(Restrictions.like("userId", tUserQueryBean.getUserId()));
			if(tUserQueryBean.getEmail() != null && !"".equals(tUserQueryBean.getEmail().trim()))
				criteria.add(Restrictions.like("email", tUserQueryBean.getEmail()));
			if(tUserQueryBean.getNickName() != null && !"".equals(tUserQueryBean.getNickName().trim()))
				criteria.add(Restrictions.like("nickName", tUserQueryBean.getNickName()));
			if(tUserQueryBean.getAccountNonExpired() != null && tUserQueryBean.getAccountNonExpired().longValue() != 0)
				criteria.add(Restrictions.eq("accountNonExpired", tUserQueryBean.getAccountNonExpired()));
			if(tUserQueryBean.getAccountNonLocked() != null && tUserQueryBean.getAccountNonLocked().longValue() != 0)
				criteria.add(Restrictions.eq("accountNonLocked", tUserQueryBean.getAccountNonLocked()));
			if(tUserQueryBean.getCredentialsNonExpired() != null && tUserQueryBean.getCredentialsNonExpired().longValue() != 0)
				criteria.add(Restrictions.eq("credentialsNonExpired", tUserQueryBean.getCredentialsNonExpired()));
			if(tUserQueryBean.getLastLoginDate() != null && !"".equals(tUserQueryBean.getLastLoginDate())){
				try{
					criteria.add(Restrictions.eq("lastLoginDate", tUserQueryBean.getLastLoginDate()));
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
			if(tUserQueryBean.getId() != null && !"".equals(tUserQueryBean.getId().trim()))
				criteria.add(Restrictions.eq("id", tUserQueryBean.getId()));
			if(tUserQueryBean.getPassword() != null && !"".equals(tUserQueryBean.getPassword().trim()))
				criteria.add(Restrictions.like("password", tUserQueryBean.getPassword()));
		}
		return query(criteria);
	}
	
	public List<TUser> queryTUserByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TUser.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<TUser> queryTUserByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TUser.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public TUser queryTUserByUserId(String userId) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(TUser.class, "t");
		criteria.add(Restrictions.eq("userId", userId));
		List<TUser> list = query(criteria);
		if(list == null || list.size() == 0) return null;
		else if(list.size() == 1){
			return (TUser)list.get(0);
		}else
			throw new Exception("Error : Multiple accounts on querying User by userId ...");
	}
	
}