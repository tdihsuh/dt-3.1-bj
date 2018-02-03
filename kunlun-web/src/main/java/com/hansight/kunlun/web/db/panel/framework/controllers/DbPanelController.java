package com.hansight.kunlun.web.db.panel.framework.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

import com.hansight.kunlun.web.db.panel.entity.DbPanel;
import com.hansight.kunlun.web.db.panel.service.DbPanelService;
import com.hansight.kunlun.web.db.panel.service.vo.DbPanelQueryBean;
import com.hansight.kunlun.web.util.BeanUtils;
import com.hansight.kunlun.web.util.RequestBeanUtils;

@Controller
@RequestMapping(value="/dbPanel")
public class DbPanelController {
	private static final Logger LOG = LoggerFactory.getLogger(DbPanelController.class);
	@Autowired
	private DbPanelService dbPanelService;
	
	@RequestMapping(value="/add.hs")
	public String add(HttpServletRequest request) throws Exception{
		return "dbPanel/dbPanelModify";
	}
	
	@RequestMapping(value="/save.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView save(@RequestBody DbPanel dbPanel) throws Exception{
		
		if(dbPanel.getId() != null && !dbPanel.getId().trim().equals("")){
			DbPanel dp = dbPanelService.get(dbPanel.getId());
			if(dp != null){
				BeanUtils.copyExcludeNull(dbPanel, dp);
				
				dbPanelService.update(dp);
				return null;
			}
		}
		
		dbPanel.setUseFlag(1L);
		dbPanel.setDateCreated(new Date());
		dbPanel.setPosition(0L);
		dbPanelService.add(dbPanel);
		return null;
	}
	
	@RequestMapping(value="/modify.hs")
	public String modify(String id,HttpServletRequest request) throws Exception{
		DbPanel dbPanel = dbPanelService.get(id);
		request.setAttribute("dbPanel",dbPanel);
		return "dbPanel/dbPanelModify";
	}
	
	@RequestMapping(value="/list.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception{
		LOG.debug("test");
		ModelAndView view = new ModelAndView();
		
		DbPanelQueryBean dbPanelQueryBean = new DbPanelQueryBean();
		RequestBeanUtils.copy(dbPanelQueryBean,request);
		List<DbPanel> list = dbPanelService.list(dbPanelQueryBean);
		view.addObject("panelList",list);
		return view;
	}
	
	@RequestMapping(value="/detail.hs")
	public String detail(String id,HttpServletRequest request) throws Exception{
		DbPanel dbPanel = dbPanelService.get(id);
		request.setAttribute("dbPanel",dbPanel);
		return "dbPanel/dbPanelDetail";
	}
	
	@RequestMapping(value="/del.hs")
	@ResponseBody
	public ModelAndView del(@RequestBody DbPanel dbPanel) throws Exception{
		dbPanelService.delete(dbPanel);
		return null;
	}
	
	@RequestMapping(value="/dels.hs")
	public String dels(HttpServletRequest request) throws Exception{
		String[] ids = request.getParameterValues("ids");
		dbPanelService.dels(ids);
		return "dbPanel/dbPanelList";
	}
	
	@RequestMapping(value="/updatePosition.hs",method=RequestMethod.POST,headers = "content-type=application/json")
	@ResponseBody
	public ModelAndView updatePosition(@RequestBody List<Map<String,String>> list) throws Exception{
		String id;
		Long position;
		DbPanel dbPanel;
		List<DbPanel> panelList = new ArrayList<DbPanel>();
		for(Map<String,String> map:list){
			id = map.get("id");
			position = Long.valueOf(map.get("position"));
			
			dbPanel = dbPanelService.get(id);
			dbPanel.setPosition(position);
			
			panelList.add(dbPanel);
		}
		dbPanelService.save(panelList);
		return null;
	}
}