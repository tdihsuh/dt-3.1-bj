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

import com.hansight.kunlun.web.config.datasource.entity.ConfAgent;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.service.ConfAgentService;
import com.hansight.kunlun.web.config.datasource.service.ConfDatasourceService;
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
@RequestMapping(value = "/agent")
public class AgentController {
	private static final Logger LOG = LoggerFactory
			.getLogger(AgentController.class);
	@Autowired
	private ConfAgentService confAgentService;
	@Autowired
	private ConfDatasourceService confDatasourceService;

	/*** list **/
	@RequestMapping(value = "/list.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception {
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer.parseInt(request.getParameter("pageSize"));
		ModelAndView mv = new ModelAndView();
		Pagination pm = confAgentService.queryPagination(currentPage, pageSize);
		mv.addObject("agentList", pm);
		return mv;
	}

	/*** add **/
	@RequestMapping(value = "/add.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView add(HttpServletRequest request) throws Exception {
		ConfAgent confAgent = new ConfAgent();
		confAgent.setName(new String(request.getParameter("name").getBytes(
				"ISO-8859-1"), "utf-8"));
		confAgent.setIp(request.getParameter("ip").trim());
		String description = request.getParameter("description");
		if (!description.equals("undefined")) {
			confAgent.setDescription(new String(description
					.getBytes("ISO-8859-1"), "utf-8"));
		}
		confAgent.setState("UNKNOWN");
		confAgent.setCreateDate(new Date());
		confAgentService.add(confAgent);
		ModelAndView mv = new ModelAndView();
		mv.addObject("confAgent", true);
		return mv;
	}

	/*** delete **/
	@RequestMapping(value = "/del.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView del(HttpServletRequest request) throws Exception {
		String id = request.getParameter("id").trim();
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer.parseInt(request.getParameter("pageSize"));
		ModelAndView mv = new ModelAndView();
		ConfAgent confAgent = confAgentService.get(id);
		List<ConfDatasource> datasource = confDatasourceService
				.queryDatasourceAgent(id);
		boolean flag = true;
		boolean flag1 = true;
		if (datasource.size() == 0) {
			confAgentService.delete(confAgent);
			/*** 删除ZooKeeper中agent ****/
			flag1 = Config.deleteConfAgent(id);
		} else {
			flag = false;
		}
		Pagination pm = confAgentService.queryPagination(currentPage, pageSize);
		mv.addObject("agentList", pm);
		mv.addObject("flag1", flag1);
		mv.addObject("flag", flag);
		return mv;
	}

	/*** detail **/
	@RequestMapping(value = "/detail.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView detail(@RequestParam("id") String id) throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfAgent confAgent = confAgentService.get(id);
		mv.addObject("confAgent", confAgent);
		return mv;
	}

	/*** update **/
	@RequestMapping(value = "/update.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView update(HttpServletRequest request) throws Exception {
		String id = request.getParameter("id");
		String state = request.getParameter("state");
		String createDate = request.getParameter("createDate");
		String description = request.getParameter("description");
		ModelAndView mv = new ModelAndView();

		ConfAgent confAgent = new ConfAgent();
		confAgent.setId(id);
		confAgent.setName(new String(request.getParameter("name").getBytes(
				"ISO-8859-1"), "utf-8").trim());
		confAgent.setIp(request.getParameter("ip").trim());
		if (!"null".equals(description)) {
			confAgent.setDescription(new String(description
					.getBytes("ISO-8859-1"), "utf-8").trim());
		}

		confAgent.setState(state);
		confAgent.setCreateDate(new Date(Long.parseLong(createDate)));
		confAgentService.update(confAgent);
		mv.addObject("confAgent", true);
		return mv;
	}

	/*** detail **/
	@RequestMapping(value = "/agentNameValid.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView agentNameValid(HttpServletRequest request)
			throws Exception {
		String name = request.getParameter("name").trim();
		ModelAndView mv = new ModelAndView();
		boolean agentName = confAgentService.AgentNameValid(name);
		mv.addObject("confAgent", agentName);
		return mv;
	}

	/*** agentLoad **/
	@RequestMapping(value = "/agentLoad.hs", method = RequestMethod.GET)
	@ResponseBody
	public String agentLoad(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		String id = request.getParameter("id").trim();
		ConfAgent confAgent = confAgentService.get(id);
		String path = WriteConfig.path() + WriteConstants.PATH;
		WriteConfig.delAllFile(path.substring(1, path.length()));
		WriteConfig.agentWrite(confAgent.getId(), WriteConstants.PATH,
				confAgent.getName());
		WriteConfig
				.downLoad(WriteConstants.PATH, confAgent.getName(), response);
		return null;
	}

}