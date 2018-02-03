package com.hansight.kunlun.web.config.datasource.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.datasource.entity.ConfAgent;
import com.hansight.kunlun.web.config.datasource.service.dao.ConfAgentDao;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfAgentQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Pagination;

/**
 * @author tao_zhang
 * @date 2014年8月18日 CONF_AGENT表对应Service的增、删、改、查
 */
@Service("confAgentService")
public class ConfAgentService {
	@Autowired
	private ConfAgentDao confAgentDao;

	public Serializable add(ConfAgent confAgent) throws Exception {
		return confAgentDao.save(confAgent);
	}

	public void update(ConfAgent confAgent) throws Exception {
		confAgentDao.update(confAgent);
	}

	public void updateJob(ConfAgent confAgent) throws Exception {
		confAgentDao.update(confAgent);
	}

	public void delete(ConfAgent confAgent) throws Exception {
		confAgentDao.delete(confAgent);
	}

	public ConfAgent get(String id) throws Exception {
		return (ConfAgent) confAgentDao.getConfAgentById(id);
	}

	public List<ConfAgent> list(ConfAgentQueryBean confAgentQueryBean)
			throws Exception {
		return confAgentDao.queryAgentConf(confAgentQueryBean);
	}

	public List<ConfAgent> queryConfAgentByIds(String[] ids) throws Exception {
		return confAgentDao.queryConfAgentByIds(ids);
	}

	public List<ConfAgent> queryConfAgentByIds(List<String> ids)
			throws Exception {
		return confAgentDao.queryConfAgentByIds(ids);
	}

	public void dels(String[] ids) throws Exception {
		if (ids == null || ids.length == 0)
			return;
		List<ConfAgent> list = queryConfAgentByIds(ids);
		dels(list);
	}

	public void dels(List<ConfAgent> list) throws Exception {
		if (list == null || list.size() == 0)
			return;
		confAgentDao.deleteAll(list);
	}

	public boolean AgentNameValid(String name) throws Exception {
		ConfAgentQueryBean confAgent = new ConfAgentQueryBean();
		confAgent.setName(name);
		return confAgentDao.agentNameValid(name);
	}
	
	public Pagination queryPagination(int currentPage,int pageSize)throws Exception{
		Set<Integer>pages = new TreeSet<Integer>();
		StringBuffer hql = new StringBuffer();
		Pagination pm = new Pagination();
		ConfAgent agent = null;
		List<ConfAgent> confAgentList = new  ArrayList<ConfAgent>();
		hql.append("FROM ConfAgent");
		pm = confAgentDao.queryPagination(hql.toString(),currentPage, pageSize);
		List<ConfAgent> confAgent = pm.getList();
		if(confAgent.size()!=0){
			for (int i = 0; i < confAgent.size(); i++) {;
				agent = new ConfAgent();
				agent.setId(confAgent.get(i).getId());
				agent.setName(confAgent.get(i).getName());
				agent.setIp(confAgent.get(i).getIp());
				if(null != confAgent.get(i).getDescription()){
					agent.setDescription(confAgent.get(i).getDescription());
				}
				agent.setState(confAgent.get(i).getState());
				confAgentList.add(agent);
			}
			pm.setList(confAgentList);
			pm.setTotalCount(confAgentList.size());
			int totalPages = confAgentDao.agentTotal(pageSize);
			for (int i = 1; i <= totalPages; i++) {
				pages.add(i);
			}
			pm.setPageNum(pages);
			pm.setTotalPages(totalPages);
			pm.setPageSize(pageSize);
			pm.setCurrentPage(currentPage);
		}else{
			pm = confAgentDao.queryPagination(hql.toString(),currentPage-1, pageSize);
			List<ConfAgent> confAgent_List = pm.getList();
			for (int i = 0; i < confAgent_List.size(); i++) {;
				agent = new ConfAgent();
				agent.setId(confAgent_List.get(i).getId());
				agent.setName(confAgent_List.get(i).getName());
				agent.setIp(confAgent_List.get(i).getIp());
				if(null != confAgent_List.get(i).getDescription()){
					agent.setDescription(confAgent_List.get(i).getDescription());
				}
				agent.setState(confAgent_List.get(i).getState());
				confAgentList.add(agent);
			}
			pm.setList(confAgentList);
			pm.setTotalCount(confAgentList.size());
			int totalPages = confAgentDao.agentTotal(pageSize);
			for (int i = 1; i <= totalPages; i++) {
				pages.add(i);
			}
			pm.setPageNum(pages);
			pm.setTotalPages(totalPages);
			pm.setPageSize(pageSize);
			pm.setCurrentPage(currentPage-1);
		}
		
		return pm;
	}
}