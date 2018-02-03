package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.user.entity.TRoleAuthority;
import com.hansight.kunlun.web.base.user.service.dao.TRoleAuthorityDao;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;

@Service
public class TRoleAuthorityService{
	@Autowired
	private TRoleAuthorityDao tRoleAuthorityDao;

	public Serializable add(TRoleAuthority tRoleAuthority)throws Exception{
		return tRoleAuthorityDao.save(tRoleAuthority);
	}
	
	public void update(TRoleAuthority tRoleAuthority)throws Exception{
		tRoleAuthorityDao.update(tRoleAuthority);
	}
	
	public void save(TRoleAuthority tRoleAuthority) throws Exception{
		if(tRoleAuthority.getId() == null || tRoleAuthority.getId().trim().equals("")){
			add(tRoleAuthority);
		}else{
			update(tRoleAuthority);
		}
	}
	
	public void delete(TRoleAuthority tRoleAuthority)throws Exception{
		tRoleAuthorityDao.delete(tRoleAuthority);
	}
	
	public TRoleAuthority get(String id)throws Exception{
		return (TRoleAuthority)tRoleAuthorityDao.getTRoleAuthorityById(id);
	}
	
	public List<TRoleAuthority> list(TRoleAuthorityQueryBean tRoleAuthorityQueryBean)throws Exception{
		return tRoleAuthorityDao.queryTRoleAuthority(tRoleAuthorityQueryBean);
	}
	
	public List<TRoleAuthority> queryTRoleAuthorityByIds(String[] ids) throws Exception{
		return tRoleAuthorityDao.queryTRoleAuthorityByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TRoleAuthority> list = queryTRoleAuthorityByIds(ids);
		if(list == null || list.size() == 0) return;
		tRoleAuthorityDao.deleteAll(list);
	}
	
	public void saveRole(String roleId,String[] authorityIds) throws Exception{
		TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
		tRoleAuthorityQueryBean.setRoleId(roleId);
		List<TRoleAuthority> list = tRoleAuthorityDao.queryTRoleAuthority(tRoleAuthorityQueryBean);
		//删除角色原有的权限
		tRoleAuthorityDao.deleteAll(list);
		if(authorityIds == null || authorityIds.length == 0) return;
		TRoleAuthority ra;
		for(String authorityId:authorityIds){
			ra = new TRoleAuthority();
			ra.setRoleId(roleId);
			ra.setAuthorityId(authorityId);
			
			save(ra);
		}
	}
	
	public void saveAuthority(String authorityId,String[] roleIds) throws Exception{
		TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
		tRoleAuthorityQueryBean.setAuthorityId(authorityId);
		List<TRoleAuthority> list = tRoleAuthorityDao.queryTRoleAuthority(tRoleAuthorityQueryBean);
		//删除角色原有的权限
		tRoleAuthorityDao.deleteAll(list);
		if(roleIds == null || roleIds.length == 0) return;
		TRoleAuthority ra;
		for(String roleId:roleIds){
			ra = new TRoleAuthority();
			ra.setRoleId(roleId);
			ra.setAuthorityId(authorityId);
			
			save(ra);
		}
	}

	public List<TRoleAuthority> findTRoleAuthorityByRoleIds(List<String> roleIds) throws Exception{
		// TODO Auto-generated method stub
		return tRoleAuthorityDao.findTRoleAuthorityByRoleIds(roleIds);
	}
}