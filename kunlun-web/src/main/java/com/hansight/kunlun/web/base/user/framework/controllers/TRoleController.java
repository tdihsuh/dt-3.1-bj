package com.hansight.kunlun.web.base.user.framework.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.entity.TRoleAuthority;
import com.hansight.kunlun.web.base.user.service.TAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRoleAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRoleService;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRoleQueryBean;
import com.hansight.kunlun.web.util.BeanUtils;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/tRole")
public class TRoleController {
	@Autowired
	private TRoleService tRoleService;
	@Autowired
	private TRoleAuthorityService tRoleAuthorityService;
	@Autowired
	private TAuthorityService tAuthorityService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		return "tRole/tRoleModify";
	}
	
	@RequestMapping(value="/save.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView save(@RequestBody TRole role) throws Exception{
		ModelAndView view = new ModelAndView();
		List<?> list;
		
		if(role.getId() != null && !role.getId().trim().equals("")){
			TRole tr = tRoleService.get(role.getId());
			if(tr != null){
				BeanUtils.copyExcludeNull(role, tr);
				
				tRoleService.update(tr);
				
				list = tRoleService.list(null);
				view.addObject("roleList",list);
				return view;
			}
		}
		
		role.setUseFlag(1L);
		role.setSortid(0L);
		tRoleService.add(role);
		
		list = tRoleService.list(null);
		view.addObject("roleList",list);
		return view;
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		TRole tRole = tRoleService.get(id);
		request.setAttribute("tRole",tRole);
		return "tRole/tRoleModify";
	}
	
	@RequestMapping(value="/list.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception{
		
		ModelAndView view = new ModelAndView();
		TRoleQueryBean tRoleQueryBean = new TRoleQueryBean();
		RequestBeanUtils.copy(tRoleQueryBean,request);
		
		List<?> list = tRoleService.list(tRoleQueryBean);
		view.addObject("roleList",list);
		return view;
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		TRole tRole = tRoleService.get(id);
		request.setAttribute("tRole",tRole);
		return "tRole/tRoleDetail";
	}
	
	@RequestMapping(value="/del.hs")
	@ResponseBody
	public ModelAndView del(@RequestBody TRole role) throws Exception{
		tRoleService.delete(role);
		List<?> list = tRoleService.list(null);
		ModelAndView view = new ModelAndView();
		view.addObject("roleList",list);
		return view;
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		tRoleService.dels(ids);
		return "tRole/tRoleList";
	}
	
	@RequestMapping(value="/authority.hs")
	public String authority(String id,HttpServletRequest request) throws Exception{
		TRole role = tRoleService.get(id);
		request.setAttribute("role", role);
		TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
		tRoleAuthorityQueryBean.setRoleId(id);
		List<TRoleAuthority> list = tRoleAuthorityService.list(tRoleAuthorityQueryBean);
		if(list != null && list.size() > 0){
			String[] ids = new String[list.size()];
			int n = 0;
			for(TRoleAuthority ra:list){
				ids[n++] = ra.getAuthorityId();
			}
			request.setAttribute("list", tAuthorityService.queryTAuthorityByIds(ids));
		}
		TAuthorityQueryBean tAuthorityQueryBean = new TAuthorityQueryBean();
		tAuthorityQueryBean.setUseFlag(1L);
		request.setAttribute("authorityList", tAuthorityService.list(tAuthorityQueryBean));
		return "tRole/authorityList";
	}
}