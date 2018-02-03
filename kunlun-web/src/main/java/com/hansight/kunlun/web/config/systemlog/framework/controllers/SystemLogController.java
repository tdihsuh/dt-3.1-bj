package com.hansight.kunlun.web.config.systemlog.framework.controllers;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.base.user.entity.TUser;
import com.hansight.kunlun.web.base.user.service.TUserService;
import com.hansight.kunlun.web.config.systemlog.service.SystemLogService;
import com.hansight.kunlun.web.config.systemlog.service.vo.Pager;
import com.hansight.kunlun.web.util.JsonUtils;

@Controller
@RequestMapping(value="/systemLog")
public class SystemLogController {
	
	@Autowired
	private SystemLogService systemLogService;
	@Autowired
	private TUserService userService;
	
	@RequestMapping(value="/list.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView list(@RequestBody Pager pager, HttpServletRequest request) throws Exception{
		
		ModelAndView mv = new ModelAndView();
		TUser user = userService.getCurrentUser();
		Pager rPager = null;
		if (JsonUtils.toJson(user).contains("ROLE_ADMIN") && JsonUtils.toJson(user).contains("管理员")) {
			rPager = systemLogService.querySystemLogsPage(pager, null);
		}else {
			rPager = systemLogService.querySystemLogsPage(pager, user.getUserId());
		}
		mv.addObject("pager", rPager);
		return mv;
	}
	@RequestMapping(value="/del.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView delete(@RequestBody Map<String, Object> data, HttpServletRequest request) throws Exception{
		Pager pager = JsonUtils.transform(data.get("pager"), Pager.class);
		List<String> ids = JsonUtils.transform(data.get("ids"), List.class);
		systemLogService.logsDeleteByIds(ids);
		ModelAndView mv = new ModelAndView();
		TUser user = userService.getCurrentUser();
		Pager rPager = null;
		if (JsonUtils.toJson(user).contains("ROLE_ADMIN") && JsonUtils.toJson(user).contains("管理员")) {
			rPager = systemLogService.querySystemLogsPage(pager, null);
		}else {
			rPager = systemLogService.querySystemLogsPage(pager, user.getUserId());
		}
		mv.addObject("pager", rPager);
		return mv;
	}

}
