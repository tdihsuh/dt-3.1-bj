package com.hansight.kunlun.web.sys.systemTimeRange.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.sys.systemTimeRange.entity.SystemTimeRange;
import com.hansight.kunlun.web.sys.systemTimeRange.service.dao.SystemTimeRangeDao;
import com.hansight.kunlun.web.sys.systemTimeRange.service.vo.SystemTimeRangeQueryBean;

@Service
public class SystemTimeRangeService{
	@Autowired
	private SystemTimeRangeDao systemTimeRangeDao;

	public Serializable add(SystemTimeRange systemTimeRange)throws Exception{
		return systemTimeRangeDao.save(systemTimeRange);
	}
	
	public void update(SystemTimeRange systemTimeRange)throws Exception{
		systemTimeRangeDao.update(systemTimeRange);
	}
	
	public void save(SystemTimeRange systemTimeRange) throws Exception{
		if(systemTimeRange.getId() == null || systemTimeRange.getId().trim().equals("")){
			add(systemTimeRange);
		}else{
			update(systemTimeRange);
		}
	}
	
	public void delete(SystemTimeRange systemTimeRange)throws Exception{
		systemTimeRangeDao.delete(systemTimeRange);
	}
	
	public SystemTimeRange get(String id)throws Exception{
		return (SystemTimeRange)systemTimeRangeDao.getSystemTimeRangeById(id);
	}
	
	public List<SystemTimeRange> list(SystemTimeRangeQueryBean systemTimeRangeQueryBean)throws Exception{
		return systemTimeRangeDao.querySystemTimeRange(systemTimeRangeQueryBean);
	}
	
	public List<SystemTimeRange> querySystemTimeRangeByIds(String[] ids) throws Exception{
		return systemTimeRangeDao.querySystemTimeRangeByIds(ids);
	}
	
	public List<SystemTimeRange> querySystemTimeRangeByIds(List<String> ids) throws Exception{
		return systemTimeRangeDao.querySystemTimeRangeByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<SystemTimeRange> list = querySystemTimeRangeByIds(ids);
		dels(list);
	}
	
	public void dels(List<SystemTimeRange> list) throws Exception{
		if(list == null || list.size() == 0) return;
		systemTimeRangeDao.deleteAll(list);
	}
	
	public SystemTimeRange queryByCategory(String category) throws Exception{
		SystemTimeRangeQueryBean systemTimeRangeQueryBean = new SystemTimeRangeQueryBean();
		systemTimeRangeQueryBean.setCategory(category);
		List<SystemTimeRange> list = list(systemTimeRangeQueryBean);
		if(list == null || list.size() == 0) return null;
		return list.get(0);
	}
}