package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.security.userdetails.CustomUserDetailsService;
import com.hansight.kunlun.web.base.user.entity.TUser;
import com.hansight.kunlun.web.base.user.service.dao.TUserDao;
import com.hansight.kunlun.web.base.user.service.vo.TUserQueryBean;

@Service
public class TUserService{
	@Autowired
	private TUserDao tUserDao;
	@Autowired
	private SaltSource saltSource;
	@Autowired
	private ShaPasswordEncoder passwordEncoder;
	@Autowired
	private CustomUserDetailsService userDetailsService;

	public Serializable add(TUser tUser)throws Exception{
		return tUserDao.save(tUser);
	}
	
	public void update(TUser tUser)throws Exception{
		tUserDao.update(tUser);
	}
	
	public void save(TUser tUser) throws Exception{
		if(tUser.getId() == null || tUser.getId().trim().equals("")){
			add(tUser);
			tUser.setPassword(getEncodePassword(tUser.getUserId(),tUser.getPassword()));
		}
		update(tUser);
	}
	
	public boolean checkPassword(TUser tUser) throws Exception{
		if(tUser == null) return false;
		String password = getEncodePassword(tUser.getUserId(), tUser.getPassword());
		TUserQueryBean tUserQueryBean = new TUserQueryBean();
		tUserQueryBean.setUserId(tUser.getUserId());
		tUserQueryBean.setPassword(password);
		
		List<TUser> list = list(tUserQueryBean);
		if(list != null && list.size() == 1) return true;
		else return false;
	}
	
	public void delete(TUser tUser)throws Exception{
		tUserDao.delete(tUser);
	}
	
	public TUser get(String id)throws Exception{
		return (TUser)tUserDao.getTUserById(id);
	}
	
	public List<TUser> list(TUserQueryBean tUserQueryBean)throws Exception{
		return tUserDao.queryTUser(tUserQueryBean);
	}
	
	public List<TUser> queryTUserByIds(String[] ids) throws Exception{
		return tUserDao.queryTUserByIds(ids);
	}
	
	public List<TUser> queryTUserByIds(List<String> ids) throws Exception{
		return tUserDao.queryTUserByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TUser> list = queryTUserByIds(ids);
		if(list == null || list.size() == 0) return;
		tUserDao.deleteAll(list);
	}
	
	public TUser queryTUserByUserId(String userId) throws Exception{
		return tUserDao.queryTUserByUserId(userId);
	}
	
	public String getCurrentUserId(){
		Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		UserDetails userDetails = null;
		if(obj instanceof UserDetails) userDetails = (UserDetails)obj;
		if(userDetails == null) return null;
		else return userDetails.getUsername();
	}
	
	public TUser getCurrentUser(String userId) throws Exception{
		if(userId == null || userId.trim().equals("")) return null;
		TUserQueryBean tUserQueryBean = new TUserQueryBean();
		tUserQueryBean.setUserId(userId);
		tUserQueryBean.setUseFlag(1L);
		List<TUser> list = tUserDao.queryTUser(tUserQueryBean);
		if(list == null || list.size() == 0) return null;
		else if(list.size() == 1) return list.get(0);
		else throw new Exception("多个相同的帐号……");
	}
	
	public TUser getCurrentUser() throws Exception{
		return getCurrentUser(getCurrentUserId());
	}
	
	public String getEncodePassword(String userId,String password){
		return passwordEncoder.encodePassword(password, saltSource.getSalt(userDetailsService.loadUserByUsername(userId)));
	}
}