package com.hansight.kunlun.web.config.systemlog.service.dao;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.hansight.kunlun.web.base.BaseDao;
import com.hansight.kunlun.web.config.systemlog.entity.SystemLog;
import com.hansight.kunlun.web.util.TimeUtils;

@Repository
public class SystemLogDao extends BaseDao<SystemLog> {
	public Serializable addSystemLog(SystemLog log) throws Exception{
		return save(log);
	}
	
	public void deleteSystemLog(SystemLog log) throws Exception{
		delete(log);
	}
	
	public void deleteAllLogs(List<SystemLog> logs) throws Exception{
		deleteAll(logs);
	}
	
	public List<SystemLog> querySysLogsAll() throws Exception{
		return find("from SystemLog");
	}
	
	public List<SystemLog> queryLogsByIds(List<String> ids) throws Exception{
		Criteria criteria = hibernateTemplate.getSessionFactory().getCurrentSession().createCriteria(SystemLog.class);
		return criteria.add(Restrictions.in("id", ids)).list();
	}
	
	public SystemLog getSystemLogById(String id) throws Exception{
		return (SystemLog) get(SystemLog.class, id);
	}
	
	public int queryTotalLogsNum() throws Exception{
		return querySysLogsAll().size();
	}
	/**
	 * 分页查询
	 * @param pageNum 分页码
	 * @param pageSize 每页最大行数
	 * @param startDate 开始日期
	 * @param endDate 截止日期
	 * @param summary 搜索内容概要
	 * @return
	 * @throws Exception
	 */
	public List<SystemLog> querySystemLogsPage(int pageNum, int pageSize, String startDate, String endDate, String summary, String user) throws Exception{
		Criteria criteria = hibernateTemplate.getSessionFactory().getCurrentSession().createCriteria(SystemLog.class);
		criteria.setFirstResult((pageNum -1) * pageSize);
		criteria.setMaxResults(pageSize);
		if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
			criteria.add(Restrictions.between("createtime", startDate, endDate));
		}
		if (!StringUtils.isEmpty(summary)) {
			criteria.add(Restrictions.or(Restrictions.like("description",
					summary, MatchMode.ANYWHERE), Restrictions.like("ip",
					summary, MatchMode.ANYWHERE)));
		}
		if (!StringUtils.isEmpty(user)) {
			criteria.add(Restrictions.eq("name", user));
		}
		criteria.addOrder(Order.desc("createtime"));
		return criteria.list();
	}
	
	public int getTotalPages(int pageSize, String startDate, String endDate, String summary, String user) throws Exception{
		Criteria criteria = hibernateTemplate.getSessionFactory().getCurrentSession().createCriteria(SystemLog.class);
		if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(endDate)) {
			criteria.add(Restrictions.between("createtime", startDate, endDate));
		}
		if (!StringUtils.isEmpty(summary)) {
			criteria.add(Restrictions.or(Restrictions.or(Restrictions.like("description",
					summary, MatchMode.ANYWHERE), Restrictions.like("ip",
					summary, MatchMode.ANYWHERE)), Restrictions.like("id",
					summary, MatchMode.ANYWHERE)));
		}
		if (!StringUtils.isEmpty(user)) {
			criteria.add(Restrictions.eq("name", user));
		}
		criteria.addOrder(Order.desc("createtime"));
		return criteria.list().size() % pageSize == 0?(criteria.list().size() / pageSize):((criteria.list().size() / pageSize) + 1);
	}
	
	public static void main(String[] args) throws Exception{
		ApplicationContext context = new FileSystemXmlApplicationContext(
				"src/main/webapp/WEB-INF/config/applicationContext.xml",
				"src/main/webapp/WEB-INF/config/spring-hibernate.xml",
				"src/main/webapp/WEB-INF/config/spring-aop-log.xml",
				"src/main/webapp/WEB-INF/config/spring-security.xml",
				"src/main/webapp/WEB-INF/config/spring-transaction.xml");
		SystemLogDao systemLogDao = (SystemLogDao)context.getBean("systemLogDao");
		SystemLog log = new SystemLog();
		log.setId("testmm");
		log.setIp("172.16.219.41");
		log.setName("test4");
		log.setResult("失败");
		log.setCreatetime(TimeUtils.format(new Date()));
		for (int i = 0; i < 19; i++) {
			log.setDescription("testmm测试用例" + i);
			systemLogDao.addSystemLog(log);
			System.out.println(i);
		}
	}
}
