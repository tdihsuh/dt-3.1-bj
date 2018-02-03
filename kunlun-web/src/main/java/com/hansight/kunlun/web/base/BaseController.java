package com.hansight.kunlun.web.base;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BaseController {
	
	@RequestMapping(value="/index.hs")
	public String index(){
		return "index";
	}
	
	@RequestMapping(value="/accessDenied.hs")
	public String accessDenied(){
		return "accessDenied";
	}
	
	@RequestMapping(value="/404.hs")
	public String error404(){
		return "404";
	}
	
	@RequestMapping(value="/500.hs")
	public String error500(){
		return "500";
	}

}
