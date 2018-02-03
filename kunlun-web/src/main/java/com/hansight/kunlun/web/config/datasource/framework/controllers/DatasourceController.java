package com.hansight.kunlun.web.config.datasource.framework.controllers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Session;
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
import com.hansight.kunlun.web.config.datasource.entity.ConfCategory;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.entity.ConfDsForwarder;
import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.datasource.service.ConfAgentService;
import com.hansight.kunlun.web.config.datasource.service.ConfCategoryService;
import com.hansight.kunlun.web.config.datasource.service.ConfDatasourceService;
import com.hansight.kunlun.web.config.datasource.service.ConfDsForwarderService;
import com.hansight.kunlun.web.config.datasource.service.ConfForwarderService;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfAgentQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfCategoryQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfCategoryVo;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfForwarderQueryBean;
import com.hansight.kunlun.web.config.datasource.util.Config;
import com.hansight.kunlun.web.config.datasource.util.Log;
import com.hansight.kunlun.web.config.datasource.util.LogAnalysis;
import com.hansight.kunlun.web.config.datasource.util.Pagination;

/**
 * @author tao_zhang
 * @date 2014年8月18日
 * 
 */
@Controller
@RequestMapping(value = "/datasource")
public class DatasourceController {
	private static final Logger LOG = LoggerFactory
			.getLogger(DatasourceController.class);
	@Autowired
	private ConfDatasourceService datasourceService;
	@Autowired
	private ConfCategoryService categoryService;
	@Autowired
	private ConfAgentService confAgentService;
	@Autowired
	private ConfForwarderService forwarderService;
	@Autowired
	private ConfDsForwarderService dsForwarderService;

	@RequestMapping(value = "/list.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView list(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer
				.parseInt(request.getParameter("pageSize"));
		Pagination pm = datasourceService.queryConfDatasource(currentPage,
				pageSize);
		mv.addObject("datasource", pm);
		return mv;
	}

	/*** delete **/
	@RequestMapping(value = "/del.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView del(HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		String id = request.getParameter("id").trim();
		int currentPage = Integer.parseInt(request.getParameter("currentPage"));
		int pageSize = Integer
				.parseInt(request.getParameter("pageSize"));
		ConfDatasource datasource = datasourceService.get(id);
		ConfCategory category = categoryService.get(datasource.getCategoryId());

		/*** 删除Zookeeper的agent ***/
//		boolean flag = Config.deleteAgent(datasource);
//		//Session sessionDs = null;
//		Session session = null;
//		if (flag) {
//			List<ConfDsForwarder> confDsForwarderList = dsForwarderService
//					.queryDsForwarder(id);
//			for (ConfDsForwarder confDsForwarder : confDsForwarderList) {
//				LOG.info(confDsForwarder.getForwarderId());
//				/*** 删除Zookeeper的forwarder ***/
//				flag = Config.deleteForwarder(datasource.getId(),
//						category.getForwarderParser(),
//						datasource.getAgentParser(), datasource.getType(),
//						datasource.getPattern(),
//						confDsForwarder.getForwarderId());
//				if (flag) {
//					sessionDs = dsForwarderService.deleteDs(confDsForwarder);
//				}
//			}
		boolean	flag = datasourceService.deleteDataSource(datasource);
//		}
//		if (!flag) {
//			if (session != null) {
//			datasourceService.rollBack(session);
//			//dsForwarderService.dsRollBack(sessionDs);
//			}
//		}
//		if (session != null) {
//			session.close();
//			//sessionDs.close();
//		}
		Pagination pm = datasourceService.queryConfDatasource(currentPage,
				pageSize);
		mv.addObject("datasource", pm);
		mv.addObject("flag", flag);
		return mv;
	}

	@RequestMapping(value = "/categoryList.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView categoryList() throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfCategoryQueryBean categoryBean = new ConfCategoryQueryBean();
		List<ConfCategory> categoryList = categoryService.list(categoryBean);
		mv.addObject("dataCategoryList", categoryList);
		return mv;
	}

	@RequestMapping(value = "/agentList.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView agentList() throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfAgentQueryBean confAgentQueryBean = new ConfAgentQueryBean();
		List<ConfAgent> List = confAgentService.list(confAgentQueryBean);
		mv.addObject("dataAgentList", List);
		return mv;
	}

	/*** list **/
	@RequestMapping(value = "/forwarderList.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView forwarderList() throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfForwarderQueryBean confForwarder = new ConfForwarderQueryBean();
		List<ConfForwarder> list = forwarderService.list(confForwarder);
		mv.addObject("dataForwarderList", list);
		return mv;
	}

	/*** get category **/
	@RequestMapping(value = "/category.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView getCategory(HttpServletRequest request)
			throws Exception {
		String id = request.getParameter("id").trim();
		ConfCategory category = categoryService.get(id);
		String sts = new String(category.getExample(), "utf-8");
		ConfCategoryVo cateVo = new ConfCategoryVo();
		cateVo.setId(category.getId());
		cateVo.setName(category.getName());
		cateVo.setAgentParser(category.getAgentParser());
		cateVo.setCreatedate(category.getCreateDate());
		cateVo.setForwarderParser(category.getForwarderParser());
		cateVo.setPattern(category.getPattern());
		cateVo.setProtocol(category.getProtocol());
		cateVo.setType(category.getType());
		cateVo.setExample(sts);
		ModelAndView mv = new ModelAndView();
		mv.addObject("category", cateVo);
		return mv;
	}

	/*** get category **/
	@RequestMapping(value = "/agent.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView getAgent(HttpServletRequest request) throws Exception {
		String id = request.getParameter("id").trim();
		ConfAgent agent = confAgentService.get(id);
		ModelAndView mv = new ModelAndView();
		mv.addObject("agent", agent);
		return mv;
	}

	/*** add datasource **/
	@RequestMapping(value = "/addDatasource.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView addDatasource(@RequestParam("pattern") String pattern,
			HttpServletRequest request) throws Exception {
		ModelAndView mv = new ModelAndView();
		String agentId = request.getParameter("agentId").trim();
		String categoryId = request.getParameter("categoryId").trim();
		String type = request.getParameter("type").trim();
		String forwarderParser = request.getParameter("forwarderParser").trim();
		String agentParser = request.getParameter("agentParser").trim();
		String host = request.getParameter("host").trim();
		String port = request.getParameter("port").trim();
		String protocol = request.getParameter("protocol").trim();
		String url = request.getParameter("url").trim();
		String encoding = request.getParameter("encoding").trim();
		//String forwarderId = request.getParameter("forwarderId").trim();
		String categoryName = request.getParameter("categoryName").trim();
		String datasourceName = request.getParameter("datasourceName").trim();
		LOG.info("agentId=" + agentId + ",categoryId=" + categoryId + ",type="
				+ type + ",pattern=" + pattern + ",host=" + host);
		LOG.info("port=" + port + "protocol=" + protocol + ",url=" + url+ ",encoding=" + encoding );
		LOG.info("agentParser=" + agentParser + ",forwarderParser="
				+ forwarderParser + ",categoryName=" + categoryName
				+ ",datasourceName=" + datasourceName);
		//String[] confForwarderId = forwarderId.split(",");
		ConfCategory category = categoryService.get(categoryId);
		ConfDatasource cd = new ConfDatasource();
		cd.setCategoryId(categoryId);
		cd.setAgentId(agentId);
		if (type.equals("undefined")) {
			cd.setType(category.getType());
		} else {
			cd.setType(type);
		}
		if (!host.equals("undefined")) {
			cd.setHost(host);
		}
		if (!port.equals("undefined")) {
			cd.setPort(Long.parseLong(port));
		}
		if (pattern.equals("undefined")) {
			cd.setPattern(category.getPattern());
		} else {
			cd.setPattern(pattern);
		}
		if (!url.equals("undefined")) {
			cd.setUrl(new String(request.getParameter("url").getBytes(
					"ISO-8859-1"), "utf-8").replaceAll("\\\\", "/"));
		}
		if (categoryName.equals("undefined")) {
			cd.setCategory(category.getName());
		} else {
			cd.setCategory(new String(request.getParameter("categoryName")
					.getBytes("ISO-8859-1"), "utf-8"));
		}
		cd.setAgentParser(agentParser);
		cd.setForwarderParser(forwarderParser);
		cd.setProtocol(protocol);
		cd.setEncoding(encoding);
		cd.setState("UNKNOWN");
		cd.setConfig("UNKNOWN");
		cd.setDatasourceName(new String(request.getParameter("datasourceName")
				.getBytes("ISO-8859-1"), "utf-8"));
		/**StringBuffer sb = new StringBuffer();
		for (int i = 0; i < confForwarderId.length; i++) {
			ConfForwarder forwarder = forwarderService.get(confForwarderId[i]);
			if (i == confForwarderId.length - 1) {
				sb.append(forwarder.getName());
			} else {
				sb.append(forwarder.getName()).append(",");
			}
		}
		cd.setForwarderName(sb.toString());**/
		cd.setCreateDate(new Date());
		String id = UUID.randomUUID().toString();
		cd.setId(id);
		boolean flag = datasourceService.save(cd);
		//Session sessionDs = null;
//		boolean flag = Config.addAgent(cd);
//		if (flag) {
//			for (int i = 0; i < confForwarderId.length; i++) {
//				ConfDsForwarder confDs = new ConfDsForwarder();
//				/**** 给Zookeeper中加forwarder ***/
//				flag = Config.addForwarder(cd.getId(), cd.getCategory(),
//						cd.getForwarderParser(), cd.getType(), cd.getPattern(),
//						confForwarderId[i].trim());
//				if (flag) {
//					confDs.setDatasourceId(cd.getId());
//					confDs.setForwarderId(confForwarderId[i]);
//					sessionDs = dsForwarderService.saveDs(confDs);
//				} else {
//					datasourceService.rollBack(session);
//					if (sessionDs != null) {
//						dsForwarderService.dsRollBack(sessionDs);
//					}
//				}
//			}
//			if (session != null ) {
//				session.close();
//				//sessionDs.close();
//			}
//		} else {
//			datasourceService.rollBack(session);
//		}
		mv.addObject("flag", flag);
		return mv;
	}

	/*** detail **/
	@RequestMapping(value = "/editDatasource.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView DatasourceEdit(@RequestParam("id") String id)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		ConfDatasource confDatasource = datasourceService.get(id);
		mv.addObject("confDatasource", confDatasource);
		return mv;
	}

	/*** detail **/
	@RequestMapping(value = "/confDsForwarderList.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView confDsForwarderList(@RequestParam("id") String id)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		StringBuffer sb = new StringBuffer();
		List<ConfDsForwarder> confDsForwarder = dsForwarderService
				.queryDsForwarder(id);
		sb.append("[");
		for (int i = 0; i < confDsForwarder.size(); i++) {
			ConfDsForwarder obj = confDsForwarder.get(i);
			if (i == confDsForwarder.size() - 1) {
				sb.append("'").append(obj.getForwarderId()).append("'");
			} else {
				sb.append("'").append(obj.getForwarderId()).append("',");
			}
		}
		sb.append("]");
		mv.addObject("confDsForwarderList", sb.toString());
		return mv;
	}

	/*** update ConfDSForwarder **/
	@SuppressWarnings("unused")
	@RequestMapping(value = "/updateDatasource.hs", method = RequestMethod.POST)
	@ResponseBody
	public ModelAndView updateDatasource(
			@RequestParam("pattern") String pattern, HttpServletRequest request)
			throws Exception {
		ModelAndView mv = new ModelAndView();
		String id = request.getParameter("id").trim();
		String agentId = request.getParameter("agentId").trim();
		String categoryId = request.getParameter("categoryId").trim();
		String categoryNa = request.getParameter("category").trim();
		String type = request.getParameter("type").trim();
		String forwarderParser = request.getParameter("forwarderParser").trim();
		String agentParser = request.getParameter("agentParser").trim();
		String host = request.getParameter("host").trim();
		String port = request.getParameter("port").trim();
		String protocol = request.getParameter("protocol").trim();
		String url = request.getParameter("url").trim();
		String encoding = request.getParameter("encoding").trim();
		String state = request.getParameter("state").trim();
		//String forwarderId = request.getParameter("forwarderId");
		String createDate = request.getParameter("createDate").trim();
		String datasourceName = request.getParameter("datasourceName").trim();
		String config = request.getParameter("config").trim();
		LOG.info("agentId=" + agentId + ",categoryId=" + categoryId + ",type="
				+ type + ",pattern=" + pattern + ",host=" + host);
		LOG.info("port=" + port + ",protocol=" + protocol + ",url=" + url+ ",encoding=" + encoding);
		LOG.info("agentParser=" + agentParser + ",forwarderParser="
				+ forwarderParser + ",datasourceName=" + datasourceName);
		LOG.info("state=" + state + ",createDate=" + createDate + ",config="
				+ config);
		//String[] confForwarderId = forwarderId.split(",");
		ConfCategory category = categoryService.get(categoryId);
		ConfDatasource cd = new ConfDatasource();
		cd.setId(id);
		cd.setCategoryId(categoryId);
		cd.setAgentId(agentId);
		cd.setType(type);
		cd.setPattern(pattern);
		if ("file".equals(protocol)) {
			if (null != url) {
				cd.setUrl(new String(request.getParameter("url").getBytes(
						"ISO-8859-1"), "utf-8").replaceAll("\\\\", "/"));
			}
		} else {
			if (null != host) {
				cd.setHost(host);
			}
			if (null != port) {
				cd.setPort(Long.parseLong(port));
			}
		}
		cd.setAgentParser(agentParser);
		cd.setForwarderParser(forwarderParser);
		cd.setProtocol(protocol);
		cd.setEncoding(encoding);
		cd.setCategory(categoryNa);
		cd.setState(state);
		/***StringBuffer sb = new StringBuffer();
		for (int i = 0; i < confForwarderId.length; i++) {
			ConfForwarder forwarder = forwarderService.get(confForwarderId[i]);
			if (i == confForwarderId.length - 1) {
				sb.append(forwarder.getName());
			} else {
				sb.append(forwarder.getName()).append(",");
			}
		}
		cd.setForwarderName(sb.toString());**/
		cd.setCreateDate(new Date(Long.parseLong(createDate)));
		cd.setDatasourceName(new String(request.getParameter("datasourceName")
				.getBytes("ISO-8859-1"), "utf-8"));
		cd.setConfig(config);
		boolean flag = datasourceService.updateDataSource(cd);
		/*** 更新Zookeeper的agent ***/
//		boolean flag = Config.updateAgent(cd);
//		if (flag) {
//			if (session != null) {
//				session.close();
//			}
//		}else{
//			datasourceService.rollBack(session);
//		}
//			Session sessionDs = null;
//			List<ConfDsForwarder> confDsForwarderList = dsForwarderService
//					.queryDsForwarder(id);
//			if (confForwarderId.length < confDsForwarderList.size()) {
//				for (int i = 0; i < confForwarderId.length; i++) {
//					ConfDsForwarder obj = confDsForwarderList.get(i);
//					if (obj.getForwarderId().equals(confForwarderId[i])) {
//						/*** 更新Zookeeper的Forwarder ***/
//						flag = Config.updateForwarder(cd.getId(),
//								cd.getCategory(), cd.getForwarderParser(),
//								cd.getType(), cd.getPattern(),
//								confForwarderId[i]);
//						if (flag) {
//							obj.setForwarderId(confForwarderId[i]);
//							sessionDs = dsForwarderService.updateDs(obj);
//						} else {
//							datasourceService.rollBack(session);
//							if (sessionDs != null) {
//								dsForwarderService.dsRollBack(sessionDs);
//							}
//						}
//					}
//				}
//				List<String> listDelete = new ArrayList<String>();
//				for (ConfDsForwarder confDsForwarder : confDsForwarderList) {
//					listDelete.add(confDsForwarder.getForwarderId());
//				}
//				for (String forwardId : confForwarderId) {
//					listDelete.remove(forwardId);
//				}
//				for (int i = 0; i < listDelete.size(); i++) {
//					Object forwarderID = listDelete.get(i);
//					/*** 更新Zookeeper的Forwarder ***/
//					flag = Config.deleteForwarder(cd.getId(), cd.getCategory(),
//							cd.getForwarderParser(), cd.getType(),
//							cd.getPattern(), forwarderID.toString());
//					if (flag) {
//						for (ConfDsForwarder confDsForwarder : confDsForwarderList) {
//							if (confDsForwarder.getForwarderId().equals(
//									forwarderID)) {
//								sessionDs = dsForwarderService
//										.deleteDs(confDsForwarder);
//							}
//						}
//					} else {
//						datasourceService.rollBack(session);
//						if (sessionDs != null) {
//							dsForwarderService.dsRollBack(sessionDs);
//						}
//					}
//				}
//			} else if (confForwarderId.length == confDsForwarderList.size()) {
//				for (int i = 0; i < confForwarderId.length; i++) {
//					ConfDsForwarder obj = confDsForwarderList.get(i);
//					/*** 更新Zookeeper的Forwarder ***/
//					flag = Config.updateForwarder(cd.getId(), cd.getCategory(),
//							cd.getForwarderParser(), cd.getType(),
//							cd.getPattern(), confForwarderId[i]);
//					if (flag) {
//						obj.setForwarderId(confForwarderId[i]);
//						sessionDs = dsForwarderService.updateDs(obj);
//					} else {
//						datasourceService.rollBack(session);
//						if (sessionDs != null) {
//							dsForwarderService.dsRollBack(sessionDs);
//						}
//					}
//				}
//			} else {
//				for (int i = 0; i < confDsForwarderList.size(); i++) {
//					ConfDsForwarder obj = confDsForwarderList.get(i);
//
//					/*** 更新Zookeeper的Forwarder ***/
//					flag = Config.updateForwarder(cd.getId(), cd.getCategory(),
//							cd.getForwarderParser(), cd.getType(),
//							cd.getPattern(), confForwarderId[i]);
//					if (flag) {
//						obj.setForwarderId(confForwarderId[i]);
//						sessionDs = dsForwarderService.updateDs(obj);
//					} else {
//						datasourceService.rollBack(session);
//						if (sessionDs != null) {
//							dsForwarderService.dsRollBack(sessionDs);
//						}
//					}
//				}
//				List<String> listAdd = new ArrayList<String>();
//				for (String forwardId : confForwarderId) {
//					listAdd.add(forwardId);
//				}
//				for (ConfDsForwarder confDsForwarder : confDsForwarderList) {
//					listAdd.remove(confDsForwarder.getForwarderId());
//				}
//
//				for (int i = 0; i < listAdd.size(); i++) {
//					Object forwarderID = listAdd.get(i);
//					ConfDsForwarder confDs = new ConfDsForwarder();
//					/**** 给Zookeeper添加forwarder ****/
//					flag = Config.addForwarder(cd.getId(), cd.getCategory(),
//							cd.getForwarderParser(), cd.getType(),
//							cd.getPattern(), forwarderID.toString());
//					if (flag) {
//						confDs.setDatasourceId(cd.getId());
//						confDs.setForwarderId(forwarderID.toString());
//						sessionDs = dsForwarderService.saveDs(confDs);
//					} else {
//						datasourceService.rollBack(session);
//						if (sessionDs != null) {
//							dsForwarderService.dsRollBack(sessionDs);
//						}
//					}
//				}
//			}
		mv.addObject("flag", flag);
		return mv;
	}

	/*** valid datasourceName **/
	@RequestMapping(value = "/datasourceNameValid.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView datasourceNameValid(HttpServletRequest request)
			throws Exception {
		String datasourceName = request.getParameter("datasourceName").trim();
		ModelAndView mv = new ModelAndView();
		boolean dataSourceName = datasourceService
				.datasourceNameValid(datasourceName);
		mv.addObject("dataSourceName", dataSourceName);
		return mv;
	}

	/*** valid url **/
	@RequestMapping(value = "/datasourceUrlValid.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView datasourceUrlValid(HttpServletRequest request)
			throws Exception {
		String url = request.getParameter("url").trim().replaceAll("\\\\", "/");
		ModelAndView mv = new ModelAndView();
		boolean dataSourceUrl = datasourceService.datasourceUrlValid(url);
		mv.addObject("dataSourceUrl", dataSourceUrl);
		return mv;
	}
	/*** valid url **/
	@RequestMapping(value = "/logBrowse.hs", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView logBrowse(@RequestParam("log") String log,HttpServletRequest request)
			throws Exception {
		String categoryId = request.getParameter("categoryId");
		//String log = request.getParameter("log");
		System.out.println(log);
		ConfCategory category = categoryService.get(categoryId);
		ModelAndView mv = new ModelAndView();
		List<Log>logBrowse = LogAnalysis.analysis(category.getName(), log);
		System.out.println(logBrowse.size());
		mv.addObject("logBrowse", logBrowse);
		return mv;
	}
}
