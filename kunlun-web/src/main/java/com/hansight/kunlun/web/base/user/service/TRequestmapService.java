package com.hansight.kunlun.web.base.user.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.base.user.entity.TAuthorityRequestmap;
import com.hansight.kunlun.web.base.user.entity.TRequestmap;
import com.hansight.kunlun.web.base.user.service.dao.TRequestmapDao;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityRequestmapQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRequestmapQueryBean;

@Service
public class TRequestmapService{
	@Autowired
	private TRequestmapDao tRequestmapDao;
	@Autowired
	private TAuthorityRequestmapService tAuthorityRequestmapService;

	public Serializable add(TRequestmap tRequestmap)throws Exception{
		return tRequestmapDao.save(tRequestmap);
	}
	
	public void update(TRequestmap tRequestmap)throws Exception{
		tRequestmapDao.update(tRequestmap);
	}
	
	public void save(TRequestmap tRequestmap,List<TAuthorityRequestmap> list) throws Exception{
		if(tRequestmap.getId() == null || tRequestmap.getId().trim().equals("")){
			add(tRequestmap);
		}else{
			update(tRequestmap);
		}
		TAuthorityRequestmapQueryBean queryBean = new TAuthorityRequestmapQueryBean();
		queryBean.setRequestmapId(tRequestmap.getId());
		List<TAuthorityRequestmap> tList = tAuthorityRequestmapService.list(queryBean);
		if(tList != null && tList.size() > 0){
			tAuthorityRequestmapService.dels(tList);
		}
		if(list != null && list.size() > 0){
			for(TAuthorityRequestmap ar:list){
				ar.setRequestmapId(tRequestmap.getId());
				tAuthorityRequestmapService.add(ar);
			}
		}
	}
	
	public void delete(TRequestmap tRequestmap)throws Exception{
		tRequestmapDao.delete(tRequestmap);
	}
	
	public TRequestmap get(String id)throws Exception{
		return (TRequestmap)tRequestmapDao.getTRequestmapById(id);
	}
	
	public List<TRequestmap> list(TRequestmapQueryBean tRequestmapQueryBean)throws Exception{
		return tRequestmapDao.queryTRequestmap(tRequestmapQueryBean);
	}
	
	public List<TRequestmap> queryTRequestmapByIds(String[] ids) throws Exception{
		return tRequestmapDao.queryTRequestmapByIds(ids);
	}
	
	public List<TRequestmap> queryTRequestmapByIds(List<String> ids) throws Exception{
		return tRequestmapDao.queryTRequestmapByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<TRequestmap> list = queryTRequestmapByIds(ids);
		if(list == null || list.size() == 0) return;
		tRequestmapDao.deleteAll(list);
	}
	
	public List<TRequestmap> queryTRequestmapByAuthorityId(String authorityId) throws Exception{
		return tRequestmapDao.queryTRequestmapByAuthorityId(authorityId);
	}
}