package com.hansight.kunlun.web.config.warning.service.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.hansight.kunlun.web.config.systemlog.entity.SystemLog;
import com.hansight.kunlun.web.config.warning.entity.ConfWarning;

public class Total implements Serializable {
 
	/**
	 * 页数
	 */
	private int currentPageNum;
	/**
	 * 总页数
	 */
	private int totalPages;
	/**
	 * 每页行数
	 */
	private int pageSize;
	
	/**
	 * 查询内容概要
	 */
	private String fuzzy;
	/**
	 * 返回结果
	 */
	private List<ConfWarning> cws;
	/**
	 * 页数栏演示列表
	 */
	private Set<Integer> showPagesNum;
	/**
	 * 页数栏显示最大个数
	 */
	private int showPagesMax;
	
	private int showPagesOrder;
	

	public int getShowPagesOrder() {
		return showPagesOrder;
	}
	public void setShowPagesOrder(int showPagesOrder) {
		this.showPagesOrder = showPagesOrder;
	}
	public int getCurrentPageNum() {
		return currentPageNum;
	}
	public void setCurrentPageNum(int currentPageNum) {
		this.currentPageNum = currentPageNum;
	}
	public int getTotalPages() {
		return totalPages;
	}
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	
	public List<ConfWarning> getCws() {
		return cws;
	}
	public void setCws(List<ConfWarning> cws) {
		this.cws= cws;
	}
	public int getShowPagesMax() {
		return showPagesMax;
	}
	public void setShowPagesMax(int showPagesMax) {
		this.showPagesMax = showPagesMax;
	}

	public String getFuzzy() {
		return fuzzy;
	}
	public void setFuzzy(String fuzzy) {
		this.fuzzy = fuzzy;
	}
	public Set<Integer> getShowPagesNum() {
		return showPagesNum;
	}
	public void setShowPagesNum(Set<Integer> showPagesNum) {
		this.showPagesNum = showPagesNum;
	}
}
