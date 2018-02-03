package com.hansight.kunlun.web.base.user.framework.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.entity.TUser;
import com.hansight.kunlun.web.base.user.service.TRoleService;
import com.hansight.kunlun.web.base.user.service.TUserService;
import com.hansight.kunlun.web.base.user.service.vo.TRoleQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TUserQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TUserRoleQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TUserUI;
import com.hansight.kunlun.web.util.BeanUtils;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/tUser")
public class TUserController {
	private static final Logger LOG = LoggerFactory.getLogger(TUserController.class);
	@Autowired
	private TUserService tUserService;
	@Autowired
	private TRoleService tRoleService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		TRoleQueryBean queryBean = new TRoleQueryBean();
		queryBean.setUseFlag(1L);
		request.setAttribute("roleList",tRoleService.list(queryBean));
		
		return "tUser/tUserModify";
	}
	
	@RequestMapping(value="/save.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView save(@RequestBody TUser user) throws Exception{
		String pw = "";
		TUser tUser = null;
		if(user.getId() == null || user.getId().trim().equals("")){
			tUser = new TUser();
			tUser.setUseFlag(1L);
			tUser.setCreateDate(new Date());
		}else{
			tUser = tUserService.get(user.getId());
			if(user.getPassword() == null || user.getPassword().trim().equals("")){
				user.setPassword(tUser.getPassword());
			}else{
				pw = tUserService.getEncodePassword(tUser.getUserId(),user.getPassword());
				user.setPassword(pw);
			}
		}
		BeanUtils.copyExcludeNull(user,tUser);
		
		tUserService.save(tUser);
		
		ModelAndView view = new ModelAndView();
		view.addObject("userList",tUserService.list(null));
		
		return view;
	}
	
	@RequestMapping(value="/checkPassword.hs",method=RequestMethod.POST)
	@ResponseBody
	public boolean checkPassword(@RequestBody TUser user) throws Exception{
		
		return tUserService.checkPassword(user);
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		TUser tUser = tUserService.get(id);
		
		TRoleQueryBean queryBean = new TRoleQueryBean();
		queryBean.setUseFlag(1L);
		List<TRole> roleList = tRoleService.list(queryBean);
		
		TUserRoleQueryBean queryBean1 = new TUserRoleQueryBean();
		queryBean1.setUserId(id);
//		List<TUserRole> userRoleList = tUserRoleService.list(queryBean1);
		
		request.setAttribute("tUser",tUser);
		request.setAttribute("roleList",roleList);
//		request.setAttribute("userRoleList",userRoleList);
		return "tUser/tUserModify";
	}
	
	@RequestMapping(value="/list.hs")
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception{
		
		ModelAndView view = new ModelAndView();
		
		TUserQueryBean tUserQueryBean = new TUserQueryBean();
		RequestBeanUtils.copy(tUserQueryBean,request);
		
		view.addObject("userList",tUserService.list(tUserQueryBean));
		
		return view;
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		TUser tUser = tUserService.get(id);
		request.setAttribute("tUser",tUser);
		return "tUser/tUserDetail";
	}
	
	@RequestMapping(value="/del.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView del(@RequestBody TUser user) throws Exception{
		tUserService.delete(user);
		
		ModelAndView view = new ModelAndView();
		
		view.addObject("userList",tUserService.list(null));
		
		return view;
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		tUserService.dels(ids);
		return "tUser/tUserList";
	}
	
	@RequestMapping(value="/json/list.hs",method=RequestMethod.GET)
	@ResponseBody
	public ModelAndView jsonList(HttpServletRequest request) throws Exception{
		TUserQueryBean tUserQueryBean = new TUserQueryBean();
		RequestBeanUtils.copy(tUserQueryBean,request);
		
		List<?> list = tUserService.list(tUserQueryBean);
		
		//request.setAttribute("tUserList",list);
		ModelAndView mav = new ModelAndView("index2");
		mav.addObject("list",list);
		
		return mav;
	}
	
	@RequestMapping(value="/current.hs",method=RequestMethod.POST)
	@ResponseBody
	public TUserUI current() throws Exception{
		TUserUI user = new TUserUI();
		TUser tUser = tUserService.getCurrentUser();
		BeanUtils.copyExcludeNull(tUser, user);
		return user;
	}
	
	@RequestMapping(value="/isLogin.hs",method=RequestMethod.POST)
	@ResponseBody
	public boolean isLogin() throws Exception{
		String userId = tUserService.getCurrentUserId();
		if(userId == null || userId.equals("")) return false;
		else return true;
	}
}