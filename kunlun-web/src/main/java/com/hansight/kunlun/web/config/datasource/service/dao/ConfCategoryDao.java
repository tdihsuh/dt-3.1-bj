package com.hansight.kunlun.web.config.datasource.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.datasource.entity.ConfCategory;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfCategoryQueryBean;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_CATEGORY表对应的DAO
 */
@Repository
public class ConfCategoryDao extends BaseDao<ConfCategory>{
	
	public Serializable addConfCategory(ConfCategory confCategory)throws Exception{
		return save(confCategory);
	}
	
	public void updateConfCategory(ConfCategory confCategory)throws Exception{
		update(confCategory);
	}
	
	public void deleteConfCategory(ConfCategory confCategory)throws Exception{
		delete(confCategory);
	}
	
	public ConfCategory getConfCategoryById(String id)throws Exception{
		return (ConfCategory)get(ConfCategory.class, id);
	}

	public List<ConfCategory> queryConfCategory(ConfCategoryQueryBean confCategoryQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfCategory.class, "t");
		if(confCategoryQueryBean != null){
			if(confCategoryQueryBean.getName() != null && !"".equals(confCategoryQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", confCategoryQueryBean.getName()));
			if(confCategoryQueryBean.getId() != null && !"".equals(confCategoryQueryBean.getId().trim()))
				criteria.add(Restrictions.like("id", confCategoryQueryBean.getId()));
			if(confCategoryQueryBean.getType() != null && !"".equals(confCategoryQueryBean.getType().trim()))
				criteria.add(Restrictions.like("type", confCategoryQueryBean.getType()));
			if(confCategoryQueryBean.getAgentParser() != null && !"".equals(confCategoryQueryBean.getAgentParser().trim()))
				criteria.add(Restrictions.like("agentParser", confCategoryQueryBean.getAgentParser()));
			if(confCategoryQueryBean.getCreatedate() != null && !"".equals(confCategoryQueryBean.getCreatedate())){
				try{
					criteria.add(Restrictions.eq("createdate", confCategoryQueryBean.getCreatedate()));
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
			if(confCategoryQueryBean.getPattern() != null && !"".equals(confCategoryQueryBean.getPattern().trim()))
				criteria.add(Restrictions.like("pattern", confCategoryQueryBean.getPattern()));
			if(confCategoryQueryBean.getForwarderParser() != null && !"".equals(confCategoryQueryBean.getForwarderParser().trim()))
				criteria.add(Restrictions.like("forwarderParser", confCategoryQueryBean.getForwarderParser()));
			if(confCategoryQueryBean.getProtocol() != null && !"".equals(confCategoryQueryBean.getProtocol().trim()))
				criteria.add(Restrictions.like("protocol", confCategoryQueryBean.getProtocol()));
		}
		return query(criteria);
	}
	
	public List<ConfCategory> queryConfCategoryByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfCategory.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<ConfCategory> queryConfCategoryByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(ConfCategory.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
}