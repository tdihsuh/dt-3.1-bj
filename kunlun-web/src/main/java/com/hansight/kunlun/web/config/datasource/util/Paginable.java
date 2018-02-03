package com.hansight.kunlun.web.config.datasource.util;

/**
 * 
 * 
 * @author xiedy
 * 
 */
public interface Paginable {
	/**
	 * 
	 * 
	 * @return
	 */
	public int getTotalCount();

	/**
	 * 
	 * 
	 * @return
	 */
	public int getTotalPage();

	/**
	 * 
	 * 
	 * @return
	 */
	public int getPageSize();

	/**
	 *
	 * 
	 * @return
	 */
	public int getPageNo();

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean isFirstPage();

	/**
	 * 
	 * 
	 * @return
	 */
	public boolean isLastPage();

	
	public int getNextPage();

	/**
	 * 
	 */
	public int getPrePage();
}
