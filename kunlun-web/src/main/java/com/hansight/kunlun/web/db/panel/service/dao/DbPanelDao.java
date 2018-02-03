package com.hansight.kunlun.web.db.panel.service.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.db.panel.entity.DbPanel;
import com.hansight.kunlun.web.db.panel.service.vo.DbPanelQueryBean;

@Repository
public class DbPanelDao extends BaseDao<DbPanel>{
	
	public Serializable addDbPanel(DbPanel dbPanel)throws Exception{
		return save(dbPanel);
	}
	
	public void updateDbPanel(DbPanel dbPanel)throws Exception{
		update(dbPanel);
	}
	
	public void deleteDbPanel(DbPanel dbPanel)throws Exception{
		delete(dbPanel);
	}
	
	public DbPanel getDbPanelById(String id)throws Exception{
		return (DbPanel)get(DbPanel.class, id);
	}

	public List<DbPanel> queryDbPanel(DbPanelQueryBean dbPanelQueryBean)throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(DbPanel.class, "t");
		if(dbPanelQueryBean != null){
			if(dbPanelQueryBean.getName() != null && !"".equals(dbPanelQueryBean.getName().trim()))
				criteria.add(Restrictions.like("name", dbPanelQueryBean.getName()));
			if(dbPanelQueryBean.getId() != null && !"".equals(dbPanelQueryBean.getId().trim()))
				criteria.add(Restrictions.like("id", dbPanelQueryBean.getId()));
			if(dbPanelQueryBean.getType() != null && !"".equals(dbPanelQueryBean.getType().trim()))
				criteria.add(Restrictions.like("type", dbPanelQueryBean.getType()));
			if(dbPanelQueryBean.getPosition() != null && dbPanelQueryBean.getPosition().longValue() != 0)
				criteria.add(Restrictions.eq("position", dbPanelQueryBean.getPosition()));
			if(dbPanelQueryBean.getUserId() != null && !"".equals(dbPanelQueryBean.getUserId().trim()))
				criteria.add(Restrictions.like("userId", dbPanelQueryBean.getUserId()));
			if(dbPanelQueryBean.getUseFlag() != null && dbPanelQueryBean.getUseFlag().longValue() != 0)
				criteria.add(Restrictions.eq("useFlag", dbPanelQueryBean.getUseFlag()));
			if(dbPanelQueryBean.getDateCreated() != null && !"".equals(dbPanelQueryBean.getDateCreated())){
				try{
					criteria.add(Restrictions.eq("dateCreated", dbPanelQueryBean.getDateCreated()));
				}catch(Exception e){
					e.printStackTrace();
					throw e;
				}
			}
			criteria.addOrder(Order.asc("position"));
			criteria.addOrder(Order.asc("dateCreated"));
		}
		return query(criteria);
	}
	
	public List<DbPanel> queryDbPanelByIds(String[] ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(DbPanel.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
	public List<DbPanel> queryDbPanelByIds(List<String> ids) throws Exception{
		DetachedCriteria criteria= DetachedCriteria.forClass(DbPanel.class, "t");
		criteria.add(Restrictions.in("id", ids));
		return query(criteria);
	}
	
}