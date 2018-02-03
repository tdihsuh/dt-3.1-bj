package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.user.entity.TAuthorityRequestmap;
import com.hansight.kunlun.web.base.user.service.dao.TAuthorityRequestmapDao;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityRequestmapQueryBean;

@Service
public class TAuthorityRequestmapService{
	@Autowired
	private TAuthorityRequestmapDao tAuthorityRequestmapDao;

	public Serializable add(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		return tAuthorityRequestmapDao.save(tAuthorityRequestmap);
	}
	
	public void update(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		tAuthorityRequestmapDao.update(tAuthorityRequestmap);
	}
	
	public void save(TAuthorityRequestmap tAuthorityRequestmap) throws Exception{
		if(tAuthorityRequestmap.getId() == null || tAuthorityRequestmap.getId().trim().equals("")){
			add(tAuthorityRequestmap);
		}else{
			update(tAuthorityRequestmap);
		}
	}
	
	public void delete(TAuthorityRequestmap tAuthorityRequestmap)throws Exception{
		tAuthorityRequestmapDao.delete(tAuthorityRequestmap);
	}
	
	public TAuthorityRequestmap get(String id)throws Exception{
		return (TAuthorityRequestmap)tAuthorityRequestmapDao.getTAuthorityRequestmapById(id);
	}
	
	public List<TAuthorityRequestmap> list(TAuthorityRequestmapQueryBean tAuthorityRequestmapQueryBean)throws Exception{
		return tAuthorityRequestmapDao.queryTAuthorityRequestmap(tAuthorityRequestmapQueryBean);
	}
	
	public List<TAuthorityRequestmap> queryTAuthorityRequestmapByIds(String[] ids) throws Exception{
		return tAuthorityRequestmapDao.queryTAuthorityRequestmapByIds(ids);
	}
	
	public List<TAuthorityRequestmap> queryTAuthorityRequestmapByIds(List<String> ids) throws Exception{
		return tAuthorityRequestmapDao.queryTAuthorityRequestmapByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TAuthorityRequestmap> list = queryTAuthorityRequestmapByIds(ids);
		dels(list);
	}
	
	public void dels(List<TAuthorityRequestmap> list) throws Exception{
		if(list == null || list.size() == 0) return;
		tAuthorityRequestmapDao.deleteAll(list);
	}
	
	public void delAuthority(String authorityId,String[] requestmapIds) throws Exception{
		if(authorityId == null || authorityId.trim().equals("") || requestmapIds == null || requestmapIds.length == 0) return;
		tAuthorityRequestmapDao.delAuthority(authorityId, requestmapIds);
	}
}