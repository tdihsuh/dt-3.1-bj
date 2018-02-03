package com.hansight.kunlun.web.config.systemlog.service.vo;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.hansight.kunlun.web.config.systemlog.entity.SystemLog;

/**
 * 分页查询信息封装类
 * @author zhangshuyu
 *
 */
public class Pager implements Serializable{
	
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
	 * 查询起始时间 （yyyy-MM-dd）
	 */
	private String startDate;
	/**
	 * 查询截止日期（yyyy-MM-dd）
	 */
	private String endDate;
	/**
	 * 查询内容概要
	 */
	private String summary;
	/**
	 * 返回结果
	 */
	private List<SystemLog> logs;
	/**
	 * 页数栏演示列表
	 */
	private Set<Integer> showPagesNum;
	/**
	 * 页数栏显示最大个数
	 */
	private int showPagesMax;
	/**
	 * 
	 */
	private int showPagesOrder;

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
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public List<SystemLog> getLogs() {
		return logs;
	}
	public void setLogs(List<SystemLog> logs) {
		this.logs = logs;
	}
	public int getShowPagesMax() {
		return showPagesMax;
	}
	public void setShowPagesMax(int showPagesMax) {
		this.showPagesMax = showPagesMax;
	}
	public int getShowPagesOrder() {
		return showPagesOrder;
	}
	public void setShowPagesOrder(int showPagesOrder) {
		this.showPagesOrder = showPagesOrder;
	}
	public Set<Integer> getShowPagesNum() {
		return showPagesNum;
	}
	public void setShowPagesNum(Set<Integer> showPagesNum) {
		this.showPagesNum = showPagesNum;
	}
	
}
