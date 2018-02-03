package com.hansight.kunlun.web.config.datasource.util;

import java.util.List;
import java.util.Set;

@SuppressWarnings("serial")
public class Pagination implements java.io.Serializable{
	/**
	 * 当前页的数据
	 */
	private List list;
	/**
	 * 
	 * 分页显示几页
	 * **/
	private Set<Integer>pageNum;
	/**
	 * 当前页
	 * */
	private int currentPage;
	/***
	 * 总页数
	 * 
	 * */
	private int totalPages;
	/**
	 * 总条数
	 * 
	 * */
	private int totalCount;
	/***
	 * 
	 * 每页个数
	 * 
	 * */
	private int pageSize;

	
	
	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List getList() {
		return list;
	}

	public void setList(List list) {
		this.list = list;
	}

	public Set<Integer> getPageNum() {
		return pageNum;
	}

	public void setPageNum(Set<Integer> pageNum) {
		this.pageNum = pageNum;
	}
	public Pagination() {
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	
	
}
