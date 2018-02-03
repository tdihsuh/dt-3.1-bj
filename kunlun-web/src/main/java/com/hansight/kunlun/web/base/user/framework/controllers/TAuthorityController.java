package com.hansight.kunlun.web.base.user.framework.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.base.user.entity.TAuthority;
import com.hansight.kunlun.web.base.user.service.TAuthorityRequestmapService;
import com.hansight.kunlun.web.base.user.service.TAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRequestmapService;
import com.hansight.kunlun.web.base.user.service.TRoleAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRoleService;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRoleQueryBean;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/tAuthority")
public class TAuthorityController {
	@Autowired
	private TAuthorityService tAuthorityService;
	@Autowired
	private TRoleService tRoleService;
	@Autowired
	private TRoleAuthorityService tRoleAuthorityService;
	@Autowired
	private TRequestmapService tRequestmapService;
	@Autowired
	private TAuthorityRequestmapService tAuthorityRequestmapService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		return "tAuthority/tAuthorityModify";
	}
	
	@RequestMapping(value="/save.hs")
	public String save(HttpServletRequest request) throws Exception{
		TAuthority tAuthority = new TAuthority();
		RequestBeanUtils.copy(tAuthority,request);
		
		tAuthority.setUseFlag(1L);
		
		tAuthorityService.save(tAuthority);
		return "redirect:list.hs";
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		TAuthority tAuthority = tAuthorityService.get(id);
		request.setAttribute("tAuthority",tAuthority);
		return "tAuthority/tAuthorityModify";
	}
	
	@RequestMapping(value="/list.hs")
	public String list(HttpServletRequest request) throws Exception{
		TAuthorityQueryBean tAuthorityQueryBean = new TAuthorityQueryBean();
		RequestBeanUtils.copy(tAuthorityQueryBean,request);
		
		List<?> list = tAuthorityService.list(tAuthorityQueryBean);
		request.setAttribute("tAuthorityList",list);
		return "tAuthority/tAuthorityList";
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		TAuthority tAuthority = tAuthorityService.get(id);
		request.setAttribute("tAuthority",tAuthority);
		return "tAuthority/tAuthorityDetail";
	}
	
	@RequestMapping(value="/del.hs")
	public String del(String id) throws Exception{
		TAuthority tAuthority = tAuthorityService.get(id);
		tAuthorityService.delete(tAuthority);
		return "tAuthority/tAuthorityList";
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		tAuthorityService.dels(ids);
		return "tAuthority/tAuthorityList";
	}
	
	@RequestMapping(value="role.hs")
	public String role(String id,HttpServletRequest request) throws Exception{
		
		request.setAttribute("authority", tAuthorityService.get(id));
		
		TRoleQueryBean tRoleQueryBean = new TRoleQueryBean();
		tRoleQueryBean.setUseFlag(1L);
		request.setAttribute("roleList", tRoleService.list(tRoleQueryBean));
		
		TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
		tRoleAuthorityQueryBean.setAuthorityId(id);
		request.setAttribute("roleAuthorityList", tRoleAuthorityService.list(tRoleAuthorityQueryBean));
		
		return "tAuthority/role";
	}
	
	@RequestMapping(value="requestmap.hs")
	public String requestmap(String id,HttpServletRequest request) throws Exception{
		
		request.setAttribute("authority", tAuthorityService.get(id));
		request.setAttribute("requestmapList", tRequestmapService.queryTRequestmapByAuthorityId(id));
		
		return "tAuthority/requestmap";
	}
	
	/**
	 * 删除某一权限下的请求资源
	 * @param authorityId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/delRequestmap.hs")
	public String delRequestmap(String authorityId,HttpServletRequest request) throws Exception{
		String[] requestmapIds = request.getParameterValues("requestmapId");
		tAuthorityRequestmapService.delAuthority(authorityId, requestmapIds);
		return "redirect:requestmap.hs?id="+authorityId;
	}
	
	@RequestMapping(value="/get.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView get(HttpServletRequest request) throws Exception{
		ModelAndView view = new ModelAndView();
		view.addObject("authority",tAuthorityService.get(request.getParameter("id")));
		return view;
	}
	
	@RequestMapping(value="/all.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView all(HttpServletRequest request) throws Exception{
		ModelAndView view = new ModelAndView();
		TAuthorityQueryBean tAuthorityQueryBean = new TAuthorityQueryBean();
		RequestBeanUtils.copy(tAuthorityQueryBean,request);
		view.addObject("authorityList",tAuthorityService.list(tAuthorityQueryBean));
		return view;
	}
}