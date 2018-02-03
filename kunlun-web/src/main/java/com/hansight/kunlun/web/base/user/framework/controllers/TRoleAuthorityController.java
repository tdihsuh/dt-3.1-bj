package com.hansight.kunlun.web.base.user.framework.controllers;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hansight.kunlun.web.base.user.entity.TRoleAuthority;
import com.hansight.kunlun.web.base.user.service.TRoleAuthorityService;
import com.hansight.kunlun.web.base.user.service.vo.TRoleAuthorityQueryBean;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/tRoleAuthority")
public class TRoleAuthorityController {
	@Autowired
	private TRoleAuthorityService tRoleAuthorityService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		return "tRoleAuthority/tRoleAuthorityModify";
	}
	
	@RequestMapping(value="/save.hs")
	public String save(HttpServletRequest request) throws Exception{
		TRoleAuthority tRoleAuthority = new TRoleAuthority();
		RequestBeanUtils.copy(tRoleAuthority,request);
		
		tRoleAuthorityService.save(tRoleAuthority);
		return "redirect:list.hs";
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		TRoleAuthority tRoleAuthority = tRoleAuthorityService.get(id);
		request.setAttribute("tRoleAuthority",tRoleAuthority);
		return "tRoleAuthority/tRoleAuthorityModify";
	}
	
	@RequestMapping(value="/list.hs")
	public String list(HttpServletRequest request) throws Exception{
		TRoleAuthorityQueryBean tRoleAuthorityQueryBean = new TRoleAuthorityQueryBean();
		RequestBeanUtils.copy(tRoleAuthorityQueryBean,request);
		
		List<?> list = tRoleAuthorityService.list(tRoleAuthorityQueryBean);
		request.setAttribute("tRoleAuthorityList",list);
		return "tRoleAuthority/tRoleAuthorityList";
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		TRoleAuthority tRoleAuthority = tRoleAuthorityService.get(id);
		request.setAttribute("tRoleAuthority",tRoleAuthority);
		return "tRoleAuthority/tRoleAuthorityDetail";
	}
	
	@RequestMapping(value="/del.hs")
	public String del(String id) throws Exception{
		TRoleAuthority tRoleAuthority = tRoleAuthorityService.get(id);
		tRoleAuthorityService.delete(tRoleAuthority);
		return "tRoleAuthority/tRoleAuthorityList";
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		tRoleAuthorityService.dels(ids);
		return "tRoleAuthority/tRoleAuthorityList";
	}
	
	@RequestMapping(value="/saveRole.hs")
	public String saveRole(String roleId,HttpServletRequest request) throws Exception{
		
		String[] authorityIds = request.getParameterValues("authorityId");
		
		tRoleAuthorityService.saveRole(roleId, authorityIds);
		
		return "redirect:/tRole/authority.hs?id="+roleId;
	}
	
	@RequestMapping(value="/saveAuthority.hs")
	public String saveAuthority(String authorityId,HttpServletRequest request) throws Exception{
		String[] roleIds = request.getParameterValues("roleId");
		
		tRoleAuthorityService.saveAuthority(authorityId, roleIds);
		
		return "redirect:/tAuthority/role.hs?id="+authorityId;
	}
}