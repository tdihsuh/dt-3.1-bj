package com.hansight.kunlun.web.config.warning.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.warning.entity.ConfWarning;
import com.hansight.kunlun.web.config.warning.service.dao.ConfWarningDao;
import com.hansight.kunlun.web.config.warning.service.vo.Total;

          
@Service("confWarningService")
public class ConfWarningService {
	@Autowired
	private ConfWarningDao warningDao;
	
	private String inforMation;
	public List<ConfWarning> queryConfWarningAll() throws Exception{
		return warningDao.queryConfWarningAll();
	}
	
	public ConfWarning get(String id) throws Exception{
		return warningDao.getConfWarningById(id);
	}
	
	public void update(ConfWarning confWarning) throws Exception{
		warningDao.saveOrUpdate(confWarning);
	}
	
	public Total queryConfWarningPage(Total total) throws Exception{
		total.setTotalPages(warningDao.getTotalPages(total.getPageSize(),total.getFuzzy()));
		List<ConfWarning> cws = null;
		if (total.getTotalPages() == 0) {
			cws = new ArrayList<ConfWarning>(); 
		}else {
			if (total.getTotalPages() < total.getCurrentPageNum()) {
				total.setCurrentPageNum(total.getTotalPages());
			}
			cws = warningDao.queryConfWarningPage(total.getCurrentPageNum(), total.getPageSize(),total.getFuzzy());
		}
		total.setCws(cws);
		
		Set<Integer> showPages = new TreeSet<Integer>();
		if (total.getTotalPages() <= total.getShowPagesMax()*total.getShowPagesOrder()) {
			for (int spNum = total.getTotalPages(); spNum > total.getShowPagesMax()*(total.getShowPagesOrder()-1) ; spNum--) {
				showPages.add(spNum);
			}
		}else {
			for (int spNum = total.getShowPagesMax()*(total.getShowPagesOrder()-1)+1; spNum <= total.getShowPagesMax()*total.getShowPagesOrder(); spNum++) {
				showPages.add(spNum);
			}
		}
		total.setShowPagesNum(showPages);
		return total;
	  }

	public void updateReduc()throws Exception{
		// TODO Auto-generated method stub
		warningDao.updateReduc();
	}
	public ConfWarning getConfWarningByName(String name) throws Exception{
		return warningDao.getConfWarningByName(name);
	}

	public ConfWarningDao getWarningDao() {
		return warningDao;
	}

	public void setWarningDao(ConfWarningDao warningDao) {
		this.warningDao = warningDao;
	}

	public String getInforMation() {
		return inforMation;
	}

	public void setInforMation(String inforMation) {
		this.inforMation = inforMation;
	}
	
	
}
