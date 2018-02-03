package com.hansight.kunlun.web.config.datasource.service;

import java.io.Serializable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.datasource.entity.ConfCategory;
import com.hansight.kunlun.web.config.datasource.service.dao.ConfCategoryDao;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfCategoryQueryBean;


/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_CATEGORY表对应Service的增、删、改、查
 */
@Service
public class ConfCategoryService{
	@Autowired
	private ConfCategoryDao confCategoryDao;

	public Serializable add(ConfCategory confCategory)throws Exception{
		return confCategoryDao.save(confCategory);
	}
	
	public void update(ConfCategory confCategory)throws Exception{
		confCategoryDao.update(confCategory);
	}
	
	public void save(ConfCategory confCategory) throws Exception{
		if(confCategory.getId() == null || confCategory.getId().trim().equals("")){
			add(confCategory);
		}else{
			update(confCategory);
		}
	}
	
	public void delete(ConfCategory confCategory)throws Exception{
		confCategoryDao.delete(confCategory);
	}
	
	public ConfCategory get(String id)throws Exception{
		return (ConfCategory)confCategoryDao.getConfCategoryById(id);
	}
	
	public List<ConfCategory> list(ConfCategoryQueryBean confCategoryQueryBean)throws Exception{
		return confCategoryDao.queryConfCategory(confCategoryQueryBean);
	}
	
	public List<ConfCategory> queryConfCategoryByIds(String[] ids) throws Exception{
		return confCategoryDao.queryConfCategoryByIds(ids);
	}
	
	public List<ConfCategory> queryConfCategoryByIds(List<String> ids) throws Exception{
		return confCategoryDao.queryConfCategoryByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<ConfCategory> list = queryConfCategoryByIds(ids);
		dels(list);
	}
	
	public void dels(List<ConfCategory> list) throws Exception{
		if(list == null || list.size() == 0) return;
		confCategoryDao.deleteAll(list);
	}
}