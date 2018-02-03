package com.hansight.kunlun.web.sys.systemTimeRange.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.sys.systemTimeRange.entity.SystemTimeRange;
import com.hansight.kunlun.web.sys.systemTimeRange.service.vo.SystemTimeRangeQueryBean;

@Repository
public class SystemTimeRangeDao extends BaseDao<SystemTimeRange>{
	
	public Serializable addSystemTimeRange(SystemTimeRange systemTimeRange)throws Exception{
		return save(systemTimeRange);
	}
	
	public void updateSystemTimeRange(SystemTimeRange systemTimeRange)throws Exception{
		update(systemTimeRange);
	}
	
	public void deleteSystemTimeRange(SystemTimeRange systemTimeRange)throws Exception{
		delete(systemTimeRange);
	}
	
	public SystemTimeRange getSystemTimeRangeById(String id)throws Exception{
		return (SystemTimeRange)get(SystemTimeRange.class, id);
	}

	public List<SystemTimeRange> querySystemTimeRange(SystemTimeRangeQueryBean systemTimeRangeQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(SystemTimeRange.class, "t");
		if(systemTimeRangeQueryBean != null){
			if(systemTimeRangeQueryBean.getId() != null && !"".equals(systemTimeRangeQueryBean.getId().trim()))
				criteria.add(Restrictions.like("id", systemTimeRangeQueryBean.getId()));
			if(systemTimeRangeQueryBean.getCategory() != null && !"".equals(systemTimeRangeQueryBean.getCategory().trim()))
				criteria.add(Restrictions.like("category", systemTimeRangeQueryBean.getCategory()));
			if(systemTimeRangeQueryBean.getTimeValue() != null && systemTimeRangeQueryBean.getTimeValue().longValue() != 0)
				criteria.add(Restrictions.eq("timeValue", systemTimeRangeQueryBean.getTimeValue()));
			if(systemTimeRangeQueryBean.getTimeUnit() != null && !"".equals(systemTimeRangeQueryBean.getTimeUnit().trim()))
				criteria.add(Restrictions.like("timeUnit", systemTimeRangeQueryBean.getTimeUnit()));
			if(systemTimeRangeQueryBean.getUserId() != null && !"".equals(systemTimeRangeQueryBean.getUserId().trim()))
				criteria.add(Restrictions.like("userId", systemTimeRangeQueryBean.getUserId()));
			if(systemTimeRangeQueryBean.getDateUpdate() != null && !"".equals(systemTimeRangeQueryBean.getDateUpdate())){
				try{
					criteria.add(Restrictions.eq("dateUpdate", systemTimeRangeQueryBean.getDateUpdate()));
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
		}
		return query(criteria);
	}
	
	public List<SystemTimeRange> querySystemTimeRangeByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(SystemTimeRange.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<SystemTimeRange> querySystemTimeRangeByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(SystemTimeRange.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
}