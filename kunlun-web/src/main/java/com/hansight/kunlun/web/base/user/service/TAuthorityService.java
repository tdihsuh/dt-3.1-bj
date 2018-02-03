package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.user.entity.TAuthority;
import com.hansight.kunlun.web.base.user.service.dao.TAuthorityDao;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityQueryBean;

@Service
public class TAuthorityService{
	@Autowired
	private TAuthorityDao tAuthorityDao;

	public Serializable add(TAuthority tAuthority)throws Exception{
		return tAuthorityDao.save(tAuthority);
	}
	
	public void update(TAuthority tAuthority)throws Exception{
		tAuthorityDao.update(tAuthority);
	}
	
	public void save(TAuthority tAuthority) throws Exception{
		if(tAuthority.getId() == null || tAuthority.getId().trim().equals("")){
			add(tAuthority);
		}else{
			update(tAuthority);
		}
	}
	
	public void delete(TAuthority tAuthority)throws Exception{
		tAuthorityDao.delete(tAuthority);
	}
	
	public TAuthority get(String id)throws Exception{
		return (TAuthority)tAuthorityDao.getTAuthorityById(id);
	}
	
	public List<TAuthority> list(TAuthorityQueryBean tAuthorityQueryBean)throws Exception{
		return tAuthorityDao.queryTAuthority(tAuthorityQueryBean);
	}
	
	public List<TAuthority> queryTAuthorityByIds(String[] ids) throws Exception{
		return tAuthorityDao.queryTAuthorityByIds(ids);
	}
	
	public List<TAuthority> queryTAuthorityByIds(List<String> ids) throws Exception{
		return tAuthorityDao.queryTAuthorityByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TAuthority> list = queryTAuthorityByIds(ids);
		if(list == null || list.size() == 0) return;
		tAuthorityDao.deleteAll(list);
	}
}