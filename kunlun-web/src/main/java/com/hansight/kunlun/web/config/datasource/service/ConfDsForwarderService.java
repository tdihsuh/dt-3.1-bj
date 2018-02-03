package com.hansight.kunlun.web.config.datasource.service;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.entity.ConfDsForwarder;
import com.hansight.kunlun.web.config.datasource.service.dao.ConfDsForwarderDao;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDsForwarderQueryBean;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DS_FORWARDER表对应Service的增、删、改、查
 */
@Service
public class ConfDsForwarderService{
	@Autowired
	private ConfDsForwarderDao confDsForwarderDao;

	public Serializable add(ConfDsForwarder confDsForwarder)throws Exception{
		return confDsForwarderDao.save(confDsForwarder);
	}
	
	public void update(ConfDsForwarder confDsForwarder)throws Exception{
		confDsForwarderDao.update(confDsForwarder);
	}
	public void save(ConfDsForwarder confDsForwarder) throws Exception{
		if(confDsForwarder.getId() == null || confDsForwarder.getId().toString().trim().equals("")){
			add(confDsForwarder);
		}else{
			update(confDsForwarder);
		}
	}
	
	public void delete(ConfDsForwarder confDsForwarder)throws Exception{
		confDsForwarderDao.delete(confDsForwarder);
	}
	
	public ConfDsForwarder get(String id)throws Exception{
		return (ConfDsForwarder)confDsForwarderDao.getConfDsForwarderById(id);
	}
	
	public List<ConfDsForwarder> list(ConfDsForwarderQueryBean confDsForwarderQueryBean)throws Exception{
		return confDsForwarderDao.queryConfDsForwarder(confDsForwarderQueryBean);
	}
	
	public List<ConfDsForwarder> queryConfDsForwarderByIds(String[] ids) throws Exception{
		return confDsForwarderDao.queryConfDsForwarderByIds(ids);
	}
	
	public List<ConfDsForwarder> queryConfDsForwarderByIds(List<String> ids) throws Exception{
		return confDsForwarderDao.queryConfDsForwarderByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<ConfDsForwarder> list = queryConfDsForwarderByIds(ids);
		dels(list);
	}
	
	public void dels(List<ConfDsForwarder> list) throws Exception{
		if(list == null || list.size() == 0) return;
		confDsForwarderDao.deleteAll(list);
	}
	
	public void deleteDataSource(ConfDatasource datasource)throws Exception{
		String[]ids ={datasource.getId()};;
		dels(ids);
	}
	public List<ConfDsForwarder> queryDsForwarder(String datasourceId)throws Exception{
		List<ConfDsForwarder>cdSource = confDsForwarderDao.queryConfDsForwarderById(datasourceId);
		return cdSource;
	}
	public List<ConfDsForwarder> queryDsForwarderById(String forwarderId)throws Exception{
		List<ConfDsForwarder>cdSource = confDsForwarderDao.queryForwarderById(forwarderId);
		return cdSource;
	}
	public void dsRollBack(Session session)throws Exception{
		confDsForwarderDao.rollBack(session);
	}
	public Session dsSession()throws Exception{
		return confDsForwarderDao.session();
	}
	public Session saveDs(ConfDsForwarder confDsForwarder)throws Exception{
		return confDsForwarderDao.save(confDsForwarder);
	}
	public Session updateDs(ConfDsForwarder confDsForwarder)throws Exception{
		return confDsForwarderDao.updateDs(confDsForwarder);
	}
	public Session deleteDs(ConfDsForwarder confDsForwarder)throws Exception{
		return confDsForwarderDao.deleteDs(confDsForwarder);
	}
}