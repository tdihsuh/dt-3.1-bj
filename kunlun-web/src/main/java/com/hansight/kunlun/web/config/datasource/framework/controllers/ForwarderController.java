package com.hansight.kunlun.web.config.datasource.framework.controllers;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.hansight.kunlun.web.config.datasource.entity.ConfDsForwarder;
import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.datasource.service.ConfDsForwarderService;
import com.hansight.kunlun.web.config.datasource.service.ConfForwarderService;
import com.hansight.kunlun.web.config.datasource.util.Config;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
import com.hansight.kunlun.web.config.datasource.util.WriteConfig;
import com.hansight.kunlun.web.config.datasource.util.WriteConstants;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * 
 */
@Controller
@RequestMapping(value = "/forwarder")
public class ForwarderController {
	private static final Logger LOG = LoggerFactory
			.getLogger(AgentController.class);
	@Autowired
	private ConfForwarderService forwarderService;
	@Autowired
	private ConfDsForwarderService confDsForwarderService;

	/*** list **/
	@RequestMapping(value = "/list.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer.parseInt(request.getParameter("pageSize"));
		Pagination pm = forwarderService.queryPagination(currentPage, pageSize);
		mv.addObject("forwarderList", pm);
		return mv;
	}

	/*** add **/
	@RequestMapping(value = "/add.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView add(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfForwarder forwarder = new ConfForwarder();
		forwarder.setName(new String(request.getParameter("name").getBytes(
				"ISO-8859-1"), "utf-8").trim());
		forwarder.setIp(request.getParameter("ip").trim());
		String description = request.getParameter("description");
		if (!description.equals("undefined")) {
			forwarder.setDescription(new String(description
					.getBytes("ISO-8859-1"), "utf-8").trim());
		}

		forwarder.setState("UNKNOWN");
		forwarder.setCreateDate(new Date());
		forwarderService.add(forwarder);
		mv.addObject("forwarder", true);
		return mv;
	}

	/*** delete **/
	@RequestMapping(value = "/del.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView del(HttpServletRequest request) throws Exception {
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer.parseInt(request.getParameter("pageSize"));
		ModelAndView mv = new ModelAndView();
		String id = request.getParameter("id");
		ConfForwarder forwarder = forwarderService.get(id);
		List<ConfDsForwarder> dsForwarder = confDsForwarderService
				.queryDsForwarderById(id);
		boolean flag = true;
		boolean flag1 = true;
		if (dsForwarder.size() == 0) {
			forwarderService.delete(forwarder);
			/*** 删除Zookeeper中的forwarder ***/
			flag1 = Config.deleteConfForwarder(id);
		} else {
			flag = false;
		}
		Pagination pm = forwarderService.queryPagination(currentPage, pageSize);
		mv.addObject("forwarderList", pm);
		mv.addObject("flag1", flag1);
		mv.addObject("flag", flag);
		return mv;
	}

	/*** detail **/
	@RequestMapping(value = "/detail.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView detail(@RequestParam("id") String id) throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfForwarder forwarder = forwarderService.get(id);
		mv.addObject("forwarder", forwarder);
		return mv;
	}

	/*** update **/
	@RequestMapping(value = "/update.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView update(HttpServletRequest request) throws Exception {
		String id = request.getParameter("id");
		String ip = request.getParameter("ip").trim();
		String state = request.getParameter("state");
		String createDate = request.getParameter("createDate");
		ModelAndView mv = new ModelAndView();
		ConfForwarder forwarder = new ConfForwarder();
		forwarder.setId(id);
		forwarder.setName(new String(request.getParameter("name").getBytes(
				"ISO-8859-1"), "utf-8"));
		forwarder.setIp(ip);
		String description = request.getParameter("description");
		if (!"null".equals(description)) {
			forwarder.setDescription(new String(description
					.getBytes("ISO-8859-1"), "utf-8"));
		}
		forwarder.setState(state);
		forwarder.setCreateDate(new Date(Long.parseLong(createDate)));
		forwarderService.update(forwarder);
		mv.addObject("forwarder", true);
		return mv;
	}

	/*** agentLoad **/
	@RequestMapping(value = "/loadForwarder.hs", method = RequestMethod.GET)
	@ResponseBody
	public String agentLoad(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String id = request.getParameter("id");
		ConfForwarder forwarder = forwarderService.get(id);
		String path = WriteConfig.path() + WriteConstants.PATH;
		WriteConfig.delAllFile(path.substring(1, path.length()));
		WriteConfig.agentWrite(forwarder.getId(), WriteConstants.PATH,
				forwarder.getName());
		WriteConfig
				.downLoad(WriteConstants.PATH, forwarder.getName(), response);
		return null;
	}

	/*** detail **/
	@RequestMapping(value = "/forwarderNameValid.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView forwarderNameValid(HttpServletRequest request)
			throws Exception {
		String name = request.getParameter("name").trim();
		ModelAndView mv = new ModelAndView();
		boolean forwarderName = forwarderService.AgentNameValid(name);
		mv.addObject("confForwarder", forwarderName);
		return mv;
	}

}
