package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.service.dao.TRoleDao;
import com.hansight.kunlun.web.base.user.service.vo.TRoleQueryBean;

@Service
public class TRoleService{
	@Autowired
	private TRoleDao tRoleDao;

	public Serializable add(TRole tRole)throws Exception{
		return tRoleDao.save(tRole);
	}
	
	public void update(TRole tRole)throws Exception{
		tRoleDao.update(tRole);
	}
	
	public void save(TRole tRole) throws Exception{
		if(tRole.getId() == null || tRole.getId().trim().equals("")){
			add(tRole);
		}else{
			update(tRole);
		}
	}
	
	public void delete(TRole tRole)throws Exception{
		tRoleDao.delete(tRole);
	}
	
	public TRole get(String id)throws Exception{
		return (TRole)tRoleDao.getTRoleById(id);
	}
	
	public List<TRole> list(TRoleQueryBean tRoleQueryBean)throws Exception{
		return tRoleDao.queryTRole(tRoleQueryBean);
	}
	
	public List<TRole> queryTRoleByIds(String[] ids) throws Exception{
		return tRoleDao.queryTRoleByIds(ids);
	}
	public List<TRole> queryTRoleByIds(List<String> ids) throws Exception{
		return tRoleDao.queryTRoleByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TRole> list = queryTRoleByIds(ids);
		if(list == null || list.size() == 0) return;
		tRoleDao.deleteAll(list);
	}
}