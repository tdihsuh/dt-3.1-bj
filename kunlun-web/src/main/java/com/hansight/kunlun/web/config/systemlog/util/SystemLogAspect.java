package com.hansight.kunlun.web.config.systemlog.util;



import java.util.Date;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.hansight.kunlun.web.base.user.entity.TRole;
import com.hansight.kunlun.web.base.user.entity.TUser;
import com.hansight.kunlun.web.base.user.service.TUserService;
import com.hansight.kunlun.web.config.datasource.entity.ConfAgent;
import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.entity.ConfForwarder;
import com.hansight.kunlun.web.config.systemlog.entity.SystemLog;
import com.hansight.kunlun.web.config.systemlog.service.SystemLogService;
import com.hansight.kunlun.web.util.JsonUtils;
import com.hansight.kunlun.web.util.TimeUtils;

@Component
public class SystemLogAspect {
	private static final Logger LOG = LoggerFactory.getLogger(SystemLogAspect.class);
	@Autowired
	private SystemLogService logService;
	@Autowired
	private TUserService userService;
	
	/**
	 * 登录失败
	 * @param jp
	 * @param fail
	 * @throws Exception
	 */
	public void loginFail(JoinPoint jp, Throwable fail) throws Exception{
		// new log
		SystemLog log = new SystemLog();
		// 解析登录信息
		Authentication authentication = (Authentication)(jp.getArgs()[0]);
		if (authentication != null) {
			WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
			log.setName(authentication.getPrincipal().toString());
			if (authentication.getDetails() != null && details != null) {
				log.setIp(details.getRemoteAddress());
			}else {
				log.setIp("");
			}
			log.setDescription(authentication.getPrincipal().toString() + "登录失败：" + fail.getMessage());
		}
		log.setResult("失败");
		log.setCreatetime(TimeUtils.format(new java.util.Date()));
		logService.logAdd(log);
	}
	/**
	 * 登陆成功
	 * @param jp
	 * @param robj
	 * @throws Exception
	 */
	public void loginSuccess(JoinPoint jp,Object robj) throws Exception{
		// new log
		SystemLog log = new SystemLog();
		// 解析返回信息
		Authentication authentication = (Authentication)robj;
		if (authentication != null) {
			WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
			org.springframework.security.core.userdetails.User u = (org.springframework.security.core.userdetails.User)authentication.getPrincipal();
			log.setName(u.getUsername());
			if (authentication.getDetails() != null && details != null) {
				log.setIp(details.getRemoteAddress());
			}else {
				log.setIp("");
			}
			log.setDescription(u.getUsername() + "登录成功");
			log.setResult(authentication.isAuthenticated()?"成功":"失败");
		}
		log.setCreatetime(TimeUtils.format(new java.util.Date()));
		logService.logAdd(log);
	}
	/**
	 * 退出系统
	 * @param jp
	 * @throws Exception
	 */
	public void logout(JoinPoint jp) throws Exception{
		SystemLog log = new SystemLog();
		if (jp.getArgs() != null && jp.getArgs().length == 3) {
			Authentication authentication = (Authentication) jp.getArgs()[2];	
			if (authentication != null) {
				org.springframework.security.core.userdetails.User u = (org.springframework.security.core.userdetails.User)authentication.getPrincipal();
				log.setName(u!=null?u.getUsername():"");
				WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
				log.setIp(details!=null?details.getRemoteAddress():"");
			}
			log.setDescription(log.getName() + "退出系统成功");
			log.setResult("成功");
		}else {
			log.setDescription("退出系统失败");
			log.setResult("失败");
		}
		log.setCreatetime(TimeUtils.format(new Date()));
		logService.logAdd(log);
	}
	/**
	 * user用户 配置日志记录
	 * @param jp
	 * @param robj
	 * @throws Exception
	 */
	public void userLog(ProceedingJoinPoint pjp) throws Exception{
		StringBuffer description = new StringBuffer();
		String operation = pjp.getSignature().getName().trim();
		SystemLog log = null;
		if (pjp.getArgs() != null && pjp.getArgs().length>0) {
			TUser user = JsonUtils.transform(pjp.getArgs()[0], TUser.class);
			StringBuffer roles = null;
			if (user.getRoles() != null && user.getRoles().size()>0) {
				roles = new StringBuffer();
				for (TRole role : user.getRoles()) {
					roles.append(role.getDescription() + ",");
				}
				roles.setLength(roles.length()-1);
			}
			if ("delete".equals(operation)) {
				description.append("删除用户：");
			}else if("save".equals(operation)){
				if (StringUtils.isEmpty(user.getId())) {
					description.append("新增用户：");
				}else {
					description.append("更新用户：");
				}
			}
			description.append("[用户名：\"").append(user.getUserId())
					.append("\",昵称：\"").append(user.getNickName())
					.append(roles == null?"":"\",角色：\""+roles.toString()).append("\"]");
		}
		Object retVal = null;
		try {
			retVal = pjp.proceed();
			log = getLogObject(true);
			log.setDescription(description.toString());
		} catch (Throwable fail) {
			log = getLogObject(false);
			log.setDescription(description.toString().split(",昵称")[0] + "] 失败：" + fail.getMessage());
		}
		logService.logAdd(log);
	}
	/**
	 * agent记录 配置采集器日志
	 * @param pjp
	 * @throws Exception
	 */
	public void agentLog(ProceedingJoinPoint pjp) throws Exception{
		SystemLog log = null;
		try {
			pjp.proceed();
			log = getLogObject(true);
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfAgent agent = JsonUtils.transform(pjp.getArgs()[0], ConfAgent.class);
				if ("add".equals(operation)) {
					description.append("新增采集器：[\"").append(agent.getName()).append("\"]");
				}else if ("delete".equals(operation)) {
					description.append("删除采集器：[").append("名称：\"")
					.append(agent.getName()).append("\"; IP：\"")
					.append(agent.getIp()).append("\"; 描述：\"")
					.append(agent.getDescription()).append("\"]");
				}else if ("update".equals(operation)) {
					description.append("更新采集器：[").append("名称：\"")
					.append(agent.getName()).append("\"; IP：\"")
					.append(agent.getIp()).append("\"; 描述：\"")
					.append(agent.getDescription()).append("\"]");
				}
			}
			log.setDescription(description.toString());
		} catch (Throwable fail) {
			if(log==null){
				log = getLogObject(false);
			}else {
				log.setDescription("失败");
			}
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfAgent agent = JsonUtils.transform(pjp.getArgs()[0], ConfAgent.class);
				if ("add".equals(operation)) {
					description.append("新增采集器[\"").append(agent.getName()).append("\"]失败：");
				}else if ("delete".equals(operation)) {
					description.append("删除采集器[\"").append(agent.getName()).append("\"]失败：");
				}else if ("update".equals(operation)) {
					description.append("更新采集器[\"").append(agent.getName()).append("\"]失败：");
				}
			}
			description.append(fail);
			log.setDescription(description.toString());
		}
		try {
			logService.logAdd(log);
		} catch (Exception e) {
			log.setDescription("未知错误：" + e.getMessage());
			log.setResult("异常");
			logService.logAdd(log);
		}
	}
	/**
	 * 记录  forwarder配置转发器 日志
	 * @param jp
	 * @throws Exception
	 */
	public void forwarderLog(ProceedingJoinPoint pjp) throws Exception{
		SystemLog log = null;
		try {
			pjp.proceed();
			log = getLogObject(true);
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfForwarder forwarder = JsonUtils.transform(pjp.getArgs()[0], ConfForwarder.class);
				if ("add".equals(operation)) {
					description.append("新增转发器：[\"").append(forwarder.getName()).append("\"]");
				}else if ("delete".equals(operation)) {
					description.append("删除转发器：[").append("名称：\"")
					.append(forwarder.getName()).append("\"; IP：\"")
					.append(forwarder.getIp()).append("\"; 描述：\"")
					.append(forwarder.getDescription()).append("\"]");
				}else if ("update".equals(operation)) {
					description.append("更新转发器：[").append("名称：\"")
					.append(forwarder.getName()).append("\"; IP：\"")
					.append(forwarder.getIp()).append("\"; 描述：\"")
					.append(forwarder.getDescription()).append("\"]");
				}
			}
			log.setDescription(description.toString());
		} catch (Throwable fail) {
			log = getLogObject(false);
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfForwarder forwarder = JsonUtils.transform(pjp.getArgs()[0], ConfForwarder.class);
				if ("add".equals(operation)) {
					description.append("新增转发器[\"").append(forwarder.getName()).append("\"]失败：");
				}else if ("delete".equals(operation)) {
					description.append("删除转发器[\"").append(forwarder.getName()).append("\"]失败：");
				}else if ("update".equals(operation)) {
					description.append("更新转发器[\"").append(forwarder.getName()).append("\"]失败：");
				}
			}
			description.append(fail);
			log.setDescription(description.toString());
		}
		
		try {
			logService.logAdd(log);
		} catch (Exception e) {
			log.setDescription("未知错误：" + e.getMessage());
			log.setResult("异常");
			logService.logAdd(log);
		}
	}
    /**
     * 记录 datasource 配置数据源 日志
     * @param pjp
     * @throws Exception
     */
    public void datasourceLog(ProceedingJoinPoint pjp) throws Exception{
    	SystemLog log = null;
    	try {
			pjp.proceed();
			log = getLogObject(true);
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfDatasource datasource = JsonUtils.transform(pjp.getArgs()[0], ConfDatasource.class);
				if ("save".equals(operation)) {
					description.append("新增数据源：[\"").append(datasource.getDatasourceName()).append("\"]");
				}else if ("deleteDataSource".equals(operation)) {
					description.append("删除数据源：[").append("数据源名称：\"")
					.append(datasource.getDatasourceName())
					.append("\";数据源类型：\"").append(datasource.getCategory())
					.append("\"; 日志类型：\"").append(datasource.getType())
					.append("\"; 协议：\"").append(datasource.getProtocol())
					.append("\"; 路径:\"" + datasource.getUrl())
					.append("\"; 状态:\"" + datasource.getState())
					.append("\"; 配置状态:\"" + datasource.getConfig())
					.append("\";采集器解析：\"").append(datasource.getAgentParser())
					.append("\"; 转发器解析:\"").append(datasource.getForwarderParser())
					.append("\"; 主机：\"").append(datasource.getHost())
					.append("\"; 端口：\"").append(datasource.getPort())
					.append("\"; 编码：\"").append(datasource.getEncoding())
					.append("\"; 解析规则：\"").append(datasource.getPattern())
					.append("\"; 采集器：\"").append(datasource.getAgentId())
					.append("\"; 转发器：\"").append(datasource.getForwarderName()).append("\"]");
				}else if ("updateDataSource".equals(operation)) {
					description.append("修改数据源：[").append("数据源名称：\"")
					.append(datasource.getDatasourceName())
					.append("\";数据源类型：\"").append(datasource.getCategory())
					.append("\"; 日志类型：\"").append(datasource.getType())
					.append("\"; 协议：\"").append(datasource.getProtocol())
					.append("\"; 路径:\"" + datasource.getUrl())
					.append("\"; 状态:\"" + datasource.getState())
					.append("\"; 配置状态:\"" + datasource.getConfig())
					.append("\";采集器解析：\"").append(datasource.getAgentParser())
					.append("\"; 转发器解析:\"").append(datasource.getForwarderParser())
					.append("\"; 主机：\"").append(datasource.getHost())
					.append("\"; 端口：\"").append(datasource.getPort())
					.append("\"; 编码：\"").append(datasource.getEncoding())
					.append("\"; 解析规则：\"").append(datasource.getPattern())
					.append("\"; 采集器：\"").append(datasource.getAgentId())
					.append("\"; 转发器：\"").append(datasource.getForwarderName()).append("\"]");
				}
			}
			log.setDescription(description.toString());
		} catch (Throwable fail) {
			log = getLogObject(false);
			String operation = pjp.getSignature().getName().trim();
			StringBuffer description = new StringBuffer();
			if (pjp.getArgs() != null && pjp.getArgs().length>0) {
				ConfDatasource datasource = JsonUtils.transform(pjp.getArgs()[0], ConfDatasource.class);
				if ("add".equals(operation)) {
					description.append("新增数据源[\"").append(datasource.getDatasourceName()).append("\"]失败：");
				}else if ("delete".equals(operation)) {
					description.append("删除数据源[\"").append(datasource.getDatasourceName()).append("\"]失败：");
				}else if ("update".equals(operation)) {
					description.append("更新数据源[\"").append(datasource.getDatasourceName()).append("\"]失败：");
				}
			}
			description.append(fail);
			log.setDescription(description.toString());
		}
		
		try {
			logService.logAdd(log);
		} catch (Exception e) {
			log.setDescription("未知错误：" + e.getMessage());
			log.setResult("异常");
			logService.logAdd(log);
		}
	}
	/**
	 * 获取当前系统使用用户
	 * @return
	 * @throws Exception
	 */
	private TUser getCurrentUser() throws Exception{
		return userService.getCurrentUser();
	}
	/**
	 * 获取用户IP
	 * @return
	 * @throws Exception
	 */
	private String getUserIp() throws Exception{
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return "";
		}
		WebAuthenticationDetails wauth = (WebAuthenticationDetails) auth.getDetails();
		if (wauth == null) {
			return "";
		}else {
			return wauth.getRemoteAddress();
		}
	}
	/**
	 * 获取Log对象
	 * @param isSuccess
	 * @return
	 * @throws Exception
	 */
	private SystemLog getLogObject(boolean isSuccess) throws Exception{
		SystemLog log = new SystemLog();
		log.setIp(getUserIp());
		log.setName(getCurrentUser().getUserId());
		log.setResult(isSuccess?"成功":"失败");
		log.setCreatetime(TimeUtils.format(new java.util.Date()));
		return log;
	}
}
