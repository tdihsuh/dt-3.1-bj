package com.hansight.kunlun.web.config.systemlog.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.systemlog.entity.SystemLog;
import com.hansight.kunlun.web.config.systemlog.service.dao.SystemLogDao;
import com.hansight.kunlun.web.config.systemlog.service.vo.Pager;

@Service
public class SystemLogService {
	
	@Autowired
	private SystemLogDao systemLogDao;
	
	public Serializable logAdd(SystemLog log) throws Exception{
		return systemLogDao.addSystemLog(log);
	}
	
	public void logDelete(SystemLog log) throws Exception{
		systemLogDao.deleteSystemLog(log);
	}
	
	public void logsDeleteByIds(List<String> ids) throws Exception{
		systemLogDao.deleteAllLogs(systemLogDao.queryLogsByIds(ids));
	}

	public List<SystemLog> querySystemLogsAll() throws Exception{
		return systemLogDao.querySysLogsAll();
	}
	
	public SystemLog get(String id) throws Exception{
		return systemLogDao.getSystemLogById(id);
	}
	
	public Pager querySystemLogsPage(Pager pager, String user) throws Exception{
		pager.setTotalPages(systemLogDao.getTotalPages(pager.getPageSize(), pager.getStartDate(), pager.getEndDate(), pager.getSummary(), user));
		List<SystemLog> logs = null;
		if (pager.getTotalPages() == 0) {
			logs = new ArrayList<SystemLog>(); 
		}else {
			if (pager.getTotalPages() < pager.getCurrentPageNum()) {
				pager.setCurrentPageNum(pager.getTotalPages());
			}
			logs = systemLogDao.querySystemLogsPage(pager.getCurrentPageNum(), pager.getPageSize(), pager.getStartDate(), pager.getEndDate(), pager.getSummary(), user);
		}
		pager.setLogs(logs);
		
		Set<Integer> showPages = new TreeSet<Integer>();
		if (pager.getTotalPages() <= pager.getShowPagesMax()*pager.getShowPagesOrder() && pager.getTotalPages() > pager.getShowPagesMax()*(pager.getShowPagesOrder()-1)) {
			for (int spNum = pager.getTotalPages(); spNum > pager.getShowPagesMax()*(pager.getShowPagesOrder()-1) ; spNum--) {
				showPages.add(spNum);
			}
		}else if (pager.getTotalPages() <= pager.getShowPagesMax()*(pager.getShowPagesOrder()-1)) {
			for (int spNum = pager.getTotalPages(); spNum > pager.getShowPagesMax()*(pager.getShowPagesOrder()-2); spNum--) {
				showPages.add(spNum);
			}
		}else {
			for (int spNum = pager.getShowPagesMax()*(pager.getShowPagesOrder()-1)+1; spNum <= pager.getShowPagesMax()*pager.getShowPagesOrder(); spNum++) {
				showPages.add(spNum);
			}
		}
		pager.setShowPagesNum(showPages);
		return pager;
	}

}
