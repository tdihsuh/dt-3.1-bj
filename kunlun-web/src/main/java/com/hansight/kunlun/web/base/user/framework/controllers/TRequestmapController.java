package com.hansight.kunlun.web.base.user.framework.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hansight.kunlun.web.base.user.entity.TAuthorityRequestmap;
import com.hansight.kunlun.web.base.user.entity.TRequestmap;
import com.hansight.kunlun.web.base.user.service.TAuthorityRequestmapService;
import com.hansight.kunlun.web.base.user.service.TAuthorityService;
import com.hansight.kunlun.web.base.user.service.TRequestmapService;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TAuthorityRequestmapQueryBean;
import com.hansight.kunlun.web.base.user.service.vo.TRequestmapQueryBean;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/tRequestmap")
public class TRequestmapController {
	@Autowired
	private TRequestmapService tRequestmapService;
	@Autowired
	private TAuthorityService tAuthorityService;
	@Autowired
	private TAuthorityRequestmapService tAuthorityRequestmapService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		TAuthorityQueryBean queryBean = new TAuthorityQueryBean();
		queryBean.setUseFlag(1L);
		request.setAttribute("authorityList",tAuthorityService.list(queryBean));
		return "tRequestmap/tRequestmapModify";
	}
	
	@RequestMapping(value="/save.hs")
	public String save(HttpServletRequest request) throws Exception{
		TRequestmap tRequestmap = new TRequestmap();
		RequestBeanUtils.copy(tRequestmap,request);
		
		String[] authorityIds = request.getParameterValues("authorityIds");
		List<TAuthorityRequestmap> list = null;
		if(authorityIds != null && authorityIds.length > 0){
			list = new ArrayList<TAuthorityRequestmap>();
			TAuthorityRequestmap authorityRequestmap;
			for(String authorityId:authorityIds){
				authorityRequestmap = new TAuthorityRequestmap();
				authorityRequestmap.setAuthorityId(authorityId);
				
				list.add(authorityRequestmap);
			}
		}
		
		tRequestmapService.save(tRequestmap,list);
		return "redirect:list.hs";
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		TRequestmap tRequestmap = tRequestmapService.get(id);
		request.setAttribute("tRequestmap",tRequestmap);
		
		TAuthorityQueryBean queryBean = new TAuthorityQueryBean();
		queryBean.setUseFlag(1L);
		request.setAttribute("authorityList",tAuthorityService.list(queryBean));
		
		TAuthorityRequestmapQueryBean tAuthorityRequestmapQueryBean = new TAuthorityRequestmapQueryBean();
		tAuthorityRequestmapQueryBean.setRequestmapId(tRequestmap.getId());
		request.setAttribute("arList",tAuthorityRequestmapService.list(tAuthorityRequestmapQueryBean));
		
		return "tRequestmap/tRequestmapModify";
	}
	
	@RequestMapping(value="/list.hs")
	public String list(HttpServletRequest request) throws Exception{
		TRequestmapQueryBean tRequestmapQueryBean = new TRequestmapQueryBean();
		RequestBeanUtils.copy(tRequestmapQueryBean,request);
		
		List<?> list = tRequestmapService.list(tRequestmapQueryBean);
		request.setAttribute("tRequestmapList",list);
		return "tRequestmap/tRequestmapList";
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		TRequestmap tRequestmap = tRequestmapService.get(id);
		request.setAttribute("tRequestmap",tRequestmap);
		return "tRequestmap/tRequestmapDetail";
	}
	
	@RequestMapping(value="/del.hs")
	public String del(String id) throws Exception{
		TRequestmap tRequestmap = tRequestmapService.get(id);
		tRequestmapService.delete(tRequestmap);
		return "tRequestmap/tRequestmapList";
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		tRequestmapService.dels(ids);
		return "tRequestmap/tRequestmapList";
	}
}