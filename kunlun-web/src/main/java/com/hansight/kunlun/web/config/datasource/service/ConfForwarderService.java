package com.hansight.kunlun.web.config.datasource.service;

import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.datasource.service.dao.ConfForwarderDao;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfForwarderQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author tao_zhang
 * @date 2014年8月18日 CONF_FORWARDER表对应的Service的增、删、改、查
 */
@Service("confForwarderService")
public class ConfForwarderService {
	@Autowired
	private ConfForwarderDao confForwarderDao;

	public Serializable add(ConfForwarder confForwarder) throws Exception {
		return confForwarderDao.save(confForwarder);
	}

	public void update(ConfForwarder confForwarder) throws Exception {
		confForwarderDao.update(confForwarder);
	}

	public void updateJob(ConfForwarder confForwarder) throws Exception {
		confForwarderDao.update(confForwarder);
	}

	public void delete(ConfForwarder confForwarder) throws Exception {
		confForwarderDao.delete(confForwarder);
	}

	public ConfForwarder get(String id) throws Exception {
		return (ConfForwarder) confForwarderDao.getConfForwarderById(id);
	}

	public List<ConfForwarder> list(
			ConfForwarderQueryBean confForwarderQueryBean) throws Exception {
		return confForwarderDao.queryConfForwarder(confForwarderQueryBean);
	}

	public List<ConfForwarder> queryConfForwarderByIds(String[] ids)
			throws Exception {
		return confForwarderDao.queryConfForwarderByIds(ids);
	}

	public List<ConfForwarder> queryConfForwarderByIds(List<String> ids)
			throws Exception {
		return confForwarderDao.queryConfForwarderByIds(ids);
	}

	public void dels(String[] ids) throws Exception {
		if (ids == null || ids.length == 0)
			return;
		List<ConfForwarder> list = queryConfForwarderByIds(ids);
		dels(list);
	}

	public void dels(List<ConfForwarder> list) throws Exception {
		if (list == null || list.size() == 0)
			return;
		confForwarderDao.deleteAll(list);
	}

	public boolean AgentNameValid(String name) throws Exception {
		ConfForwarderQueryBean confAgent = new ConfForwarderQueryBean();
		confAgent.setName(name);
		return confForwarderDao.forwarderNameValid(name);
	}
	public Pagination queryPagination(int currentPage,int pageSize)throws Exception{
		Set<Integer>pages = new TreeSet<Integer>();
		StringBuffer hql = new StringBuffer();
		Pagination pm = new Pagination();
		ConfForwarder forwarder = null;
		List<ConfForwarder> confforwarderList = new  ArrayList<ConfForwarder>();
		hql.append("FROM ConfForwarder");
		pm = confForwarderDao.queryPagination(hql.toString(),currentPage, pageSize);
		List<ConfForwarder> confForwarder = pm.getList();
		if(confForwarder.size()!=0){
			for (int i = 0; i < confForwarder.size(); i++) {;
				forwarder = new ConfForwarder();
				forwarder.setId(confForwarder.get(i).getId());
				forwarder.setName(confForwarder.get(i).getName());
				forwarder.setIp(confForwarder.get(i).getIp());
				if(null != confForwarder.get(i).getDescription()){
					forwarder.setDescription(confForwarder.get(i).getDescription());
				}
				forwarder.setState(confForwarder.get(i).getState());
				confforwarderList.add(forwarder);
			}
			pm.setList(confforwarderList);
			pm.setTotalCount(confforwarderList.size());
			int totalPages = confForwarderDao.agentTotal(pageSize);
			for (int i = 1; i <= totalPages; i++) {
				pages.add(i);
			}
			pm.setPageNum(pages);
			pm.setTotalPages(totalPages);
			pm.setPageSize(pageSize);
			pm.setCurrentPage(currentPage);
		}else{
			pm = confForwarderDao.queryPagination(hql.toString(),currentPage-1, pageSize);
			List<ConfForwarder> confForwarder_list = pm.getList();
			for (int i = 0; i < confForwarder_list.size(); i++) {;
				forwarder = new ConfForwarder();
				forwarder.setId(confForwarder_list.get(i).getId());
				forwarder.setName(confForwarder_list.get(i).getName());
				forwarder.setIp(confForwarder_list.get(i).getIp());
				if(null != confForwarder_list.get(i).getDescription()){
					forwarder.setDescription(confForwarder_list.get(i).getDescription());
				}
				forwarder.setState(confForwarder_list.get(i).getState());
				confforwarderList.add(forwarder);
			}
			pm.setList(confforwarderList);
			pm.setTotalCount(confforwarderList.size());
			int totalPages = confForwarderDao.agentTotal(pageSize);
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
