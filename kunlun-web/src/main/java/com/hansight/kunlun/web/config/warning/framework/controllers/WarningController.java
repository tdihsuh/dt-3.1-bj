package com.hansight.kunlun.web.config.warning.framework.controllers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;


import com.hansight.kunlun.web.config.warning.entity.ConfWarning;
import com.hansight.kunlun.web.config.warning.service.ConfWarningService;
import com.hansight.kunlun.web.config.warning.service.vo.Total;
import com.hansight.kunlun.web.util.JsonUtils;

@Controller
@RequestMapping(value="/warning")
public class WarningController {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@Autowired
	private ConfWarningService confWarningService;
		
	/***list**/
	@RequestMapping(value="/list.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView list(@RequestBody Total total, HttpServletRequest request) throws Exception{
		ModelAndView mv = new ModelAndView();
		Total totalSearch= confWarningService.queryConfWarningPage(total);
		mv.addObject("total",totalSearch);
		return mv;
	}
	
	/***getInformation**/
	@RequestMapping(value="/getInformation.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView getInformation(HttpServletRequest request) throws Exception{
		ModelAndView mv = new ModelAndView();
		String inforMation = confWarningService.getInforMation();
		mv.addObject("information",inforMation);
		return mv;
	}
	
	 
	
	/***update**/
	@RequestMapping(value="/update.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView update(@RequestBody Map<String,Object> data, HttpServletRequest request) throws Exception{
		
		ModelAndView mv = new ModelAndView();
		Total total = JsonUtils.transform(data.get("total"), Total.class);
		String id = request.getParameter("id").toString().trim();
		String email = request.getParameter("email").toString().trim();
		String value = request.getParameter("value").toString().trim();
		ConfWarning cw = confWarningService.get(id);
		//Total total=request.getParameter("total");
		if(email!=null && email.toString().trim()!=""){
			cw.setEmail(email.toString().trim());
		}
		cw.setUpdateTime(sdf.format(new Date()));
		if(value!=null && value.toString().trim()!=""){
			cw.setValue(value.toString().trim());
		}
		confWarningService.update(cw);
		return  list(total,request);
	}
	
	/***edit**/
	@RequestMapping(value="/editList.hs",method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView detail(@RequestParam("id") String id) throws Exception{
		ModelAndView mv = new ModelAndView();
		ConfWarning warning = confWarningService.get(id);
		
		mv.addObject("confWarning",warning);
		return mv;
	}
	
	/***reduction**/
	@RequestMapping(value="/reduction.hs",method=RequestMethod.POST)
	@ResponseBody
	public ModelAndView reduc(@RequestBody Total total, HttpServletRequest request) throws Exception{
		ModelAndView mv = new ModelAndView();
		confWarningService.updateReduc();
		return  list(total,request);
		//mv.addObject("total",totalSearch);
		
	}
	
}
