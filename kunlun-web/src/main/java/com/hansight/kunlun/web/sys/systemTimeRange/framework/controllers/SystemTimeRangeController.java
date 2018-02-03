package com.hansight.kunlun.web.sys.systemTimeRange.framework.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.sys.systemTimeRange.entity.SystemTimeRange;
import com.hansight.kunlun.web.sys.systemTimeRange.service.SystemTimeRangeService;
import com.hansight.kunlun.web.sys.systemTimeRange.service.vo.SystemTimeRangeQueryBean;
import com.hansight.kunlun.web.util.BeanUtils;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/systemTimeRange")
public class SystemTimeRangeController {
	@Autowired
	private SystemTimeRangeService systemTimeRangeService;
	
	@RequestMapping(value="/add.html")
	public String add(HttpServletRequest request) throws Exception{
		return "systemTimeRange/systemTimeRangeModify";
	}
	
	@RequestMapping(value="/save.hs",method=RequestMethod.POST)
	@ResponseBody
	public void save(@RequestBody SystemTimeRange systemTimeRange) throws Exception{

		SystemTimeRange str = systemTimeRangeService.get(systemTimeRange.getId());
		BeanUtils.copyExcludeNull(systemTimeRange, str);
		
		systemTimeRangeService.save(str);
	}
	
	@RequestMapping(value="/modify.html")
	public String modify(String id,HttpServletRequest request) throws Exception{
		SystemTimeRange systemTimeRange = systemTimeRangeService.get(id);
		request.setAttribute("systemTimeRange",systemTimeRange);
		return "systemTimeRange/systemTimeRangeModify";
	}
	
	@RequestMapping(value="/list.html")
	public String list(HttpServletRequest request) throws Exception{
		SystemTimeRangeQueryBean systemTimeRangeQueryBean = new SystemTimeRangeQueryBean();
		RequestBeanUtils.copy(systemTimeRangeQueryBean,request);
		
		List<SystemTimeRange> list = systemTimeRangeService.list(systemTimeRangeQueryBean);
		request.setAttribute("systemTimeRangeList",list);
		return "systemTimeRange/systemTimeRangeList";
	}
	
	@RequestMapping(value="/detail.html")
	public String detail(String id,HttpServletRequest request) throws Exception{
		SystemTimeRange systemTimeRange = systemTimeRangeService.get(id);
		request.setAttribute("systemTimeRange",systemTimeRange);
		return "systemTimeRange/systemTimeRangeDetail";
	}
	
	@RequestMapping(value="/del.html")
	public String del(String id) throws Exception{
		SystemTimeRange systemTimeRange = systemTimeRangeService.get(id);
		systemTimeRangeService.delete(systemTimeRange);
		return "systemTimeRange/systemTimeRangeList";
	}
	
	@RequestMapping(value="/dels.html")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		systemTimeRangeService.dels(ids);
		return "systemTimeRange/systemTimeRangeList";
	}
	
	@RequestMapping(value="/queryByCategory.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView queryByCategory(HttpServletRequest request) throws Exception{
		String category = request.getParameter("category");
		if(category == null || category.trim().equals("")) return null;
		ModelAndView view = new ModelAndView();
		view.addObject("systemTimeRange",systemTimeRangeService.queryByCategory(category));
		return view;
	}
}