package com.hansight.kunlun.web.base.login.framework.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hansight.kunlun.web.base.user.service.TUserService;

@Controller
public class LoginController{
	
	@Autowired
	private ShaPasswordEncoder passwordEncoder;
	@Autowired
	private SaltSource saltSource;
	@Autowired
	private TUserService tUserService;
	@Autowired
	private UserDetailsService userDetailsService;

	@RequestMapping(value="/login.hs")
	public String login(){
		return "login";
	}
}