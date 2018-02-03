package com.hansight.kunlun.web.db.panel.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.db.panel.entity.DbPanel;
import com.hansight.kunlun.web.db.panel.service.dao.DbPanelDao;
import com.hansight.kunlun.web.db.panel.service.vo.DbPanelQueryBean;

@Service
public class DbPanelService{
	@Autowired
	private DbPanelDao dbPanelDao;

	public Serializable add(DbPanel dbPanel)throws Exception{
		return dbPanelDao.save(dbPanel);
	}
	
	public void update(DbPanel dbPanel)throws Exception{
		dbPanelDao.update(dbPanel);
	}
	
	public void save(DbPanel dbPanel) throws Exception{
		if(dbPanel.getId() == null || dbPanel.getId().trim().equals("")){
			add(dbPanel);
		}else{
			update(dbPanel);
		}
	}
	
	public void delete(DbPanel dbPanel)throws Exception{
		dbPanelDao.delete(dbPanel);
	}
	
	public DbPanel get(String id)throws Exception{
		return (DbPanel)dbPanelDao.getDbPanelById(id);
	}
	
	public List<DbPanel> list(DbPanelQueryBean dbPanelQueryBean)throws Exception{
		return dbPanelDao.queryDbPanel(dbPanelQueryBean);
	}
	
	public List<DbPanel> queryDbPanelByIds(String[] ids) throws Exception{
		return dbPanelDao.queryDbPanelByIds(ids);
	}
	
	public List<DbPanel> queryDbPanelByIds(List<String> ids) throws Exception{
		return dbPanelDao.queryDbPanelByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<DbPanel> list = queryDbPanelByIds(ids);
		dels(list);
	}
	
	public void dels(List<DbPanel> list) throws Exception{
		if(list == null || list.size() == 0) return;
		dbPanelDao.deleteAll(list);
	}
	
	public void save(List<DbPanel> panelList) throws Exception{
		// TODO Auto-generated method stub
		if(panelList == null) return;
		for(DbPanel dbPanel:panelList){
			save(dbPanel);
		}
	}
}