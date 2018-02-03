package com.hansight.kunlun.web.config.datasource.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hansight.kunlun.web.config.datasource.entity.ConfDatasource;
import com.hansight.kunlun.web.config.datasource.service.dao.ConfDatasourceDao;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfAgentQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDatasourceQueryBean;
import com.hansight.kunlun.web.config.datasource.service.vo.ConfDatasourceVo;
import com.hansight.kunlun.web.config.datasource.util.Config;
import com.hansight.kunlun.web.config.datasource.util.Pagination;
/**
 * @author tao_zhang
 * @date 2014年8月18日
 * CONF_DATASOURCE表对应Service的增、删、改、查
 */
@Service("confDatasourceService")
public class ConfDatasourceService {
	@Autowired
	private ConfDatasourceDao confDatasourceDao;
	public Serializable add(ConfDatasource confDatasource)throws Exception{
		return confDatasourceDao.save(confDatasource);
	}
	
	public void update(ConfDatasource confDatasource)throws Exception{
		confDatasourceDao.update(confDatasource);
	}
	public void updateJob(ConfDatasource confDatasource)throws Exception{
		confDatasourceDao.update(confDatasource);
	}	
	public void delete(ConfDatasource confDatasource)throws Exception{
		confDatasourceDao.delete(confDatasource);
	}
	
	public ConfDatasource get(String id)throws Exception{
		return (ConfDatasource)confDatasourceDao.getConfDatasourceById(id);
	}
	
	public List<ConfDatasource> list(ConfDatasourceQueryBean confDatasourceQueryBean)throws Exception{
		
		List<ConfDatasource> confDatasource= confDatasourceDao.queryConfDatasource(confDatasourceQueryBean);
		return confDatasource;
	}
	public List<ConfDatasource> queryConfDatasourceByIds(String[] ids) throws Exception{
		return confDatasourceDao.queryConfDatasourceByIds(ids);
	}
	
	public List<ConfDatasource> queryConfDatasourceByIds(List<String> ids) throws Exception{
		return confDatasourceDao.queryConfDatasourceByIds(ids);
	}
	
	public void dels(String[] ids) throws Exception{
		if(ids == null || ids.length == 0) return;
		List<ConfDatasource> list = queryConfDatasourceByIds(ids);
		dels(list);
	}
	
	public void dels(List<ConfDatasource> list) throws Exception{
		if(list == null || list.size() == 0) return;
		confDatasourceDao.deleteAll(list);
	}
	
	public List<ConfDatasource> queryDatasource2(String host,String port)throws Exception{
		List<ConfDatasource>cdSource = confDatasourceDao.queryDatasourceById2(host,port);
		return cdSource;
	}
	public List<ConfDatasource> queryDatasource(String url)throws Exception{
		List<ConfDatasource>cdSource = confDatasourceDao.queryDatasourceById(url);
		return cdSource;
	}
	public List<ConfDatasource> queryDatasourceAgent(String agentId)throws Exception{
		List<ConfDatasource>cdSource = confDatasourceDao.queryDatasourceAgentById(agentId);
		return cdSource;
	}
	
	public Pagination findByPages(int firstResult,int maxResults)throws Exception{
		ConfDatasource conf = new ConfDatasource();
		Pagination pm = (Pagination) confDatasourceDao.findByExample(conf, firstResult, maxResults);
		return pm;
	}
	public  Pagination queryConfDataSource(){
		List<ConfDatasourceVo> listDatasource = new ArrayList<ConfDatasourceVo>(); 
		Pagination pm = new Pagination();
		ConfDatasourceVo confData = null;
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT  a.name AS name,b.id AS id,b.agentId AS agentId,");
		sql.append(" b.category AS category ,b.pattern AS pattern,b.agentParser,");
		sql.append(" b.encoding,b.forwarderName,b.categoryId,b.host,b.state,b.type,b.url,b.port,b.protocol,b.forwarderParser,");
		sql.append(" b.datasourceName,b.config,b.configInfo");
		sql.append(" FROM  ConfAgent a,ConfDatasource b ");
		sql.append(" WHERE a.id = b.agentId");
		sql.append(" GROUP BY b.createDate");
		List<Object>dataList = confDatasourceDao.queryObject(sql.toString());
		if(dataList.size()!=0){
			for (int i = 0; i < dataList.size(); i++) {
				Object[] obj = (Object[]) dataList.get(i);
				confData = new ConfDatasourceVo();
				confData.setAgentName(obj[0].toString());
				confData.setId(obj[1].toString());
				confData.setAgentId(obj[2].toString());
				confData.setCategory(obj[3].toString());
				if(null != obj[4]){
					confData.setPattern(obj[4].toString());
				}
				confData.setAgentParser(obj[5].toString());
				confData.setEncoding(obj[6].toString());
				confData.setForwarderName(obj[7].toString());
				confData.setCategoryId(obj[8].toString());
				if(null != obj[9]){
					confData.setHost(obj[9].toString());
				}
				confData.setState(obj[10].toString());
				confData.setType(obj[11].toString());
				if(null != obj[12]){
					confData.setUrl(obj[12].toString());
				}
				if(null!=obj[13]){
					confData.setPort(Long.parseLong(obj[13].toString()));
				}
				confData.setProtocol(obj[14].toString());
				confData.setForwarderParser(obj[15].toString());
				confData.setDatasourceName(obj[16].toString());
				confData.setConfig(obj[17].toString());
				if(null!=obj[18]){
					confData.setConfigInfo(obj[18].toString());
				}
				listDatasource.add(confData);
			}
			pm.setList(listDatasource);
			pm.setTotalCount(listDatasource.size());
		}
		return pm;
	}
	public boolean datasourceNameValid(String datasourceName)throws Exception{
		return confDatasourceDao.datasourceNameValid(datasourceName);
	}
	public boolean datasourceUrlValid(String url)throws Exception{
		return confDatasourceDao.datasourceUrlValid(url);
	}
	public Pagination queryConfDatasource(int offSet,int pageSize)throws Exception{
		List<ConfDatasourceVo> listDatasource = new ArrayList<ConfDatasourceVo>();
		Set<Integer>pages = new TreeSet<Integer>();
		ConfDatasourceVo confData = null;
		Pagination pm = new Pagination();
		StringBuffer Hql = new StringBuffer();
		Hql.append("SELECT  a.name AS name,b.id AS id,b.agentId AS agentId,");
		Hql.append(" b.category AS category ,b.pattern AS pattern,b.agentParser,");
		Hql.append(" b.encoding,b.forwarderName,b.categoryId,b.host,b.state,b.type,b.url,b.port,b.protocol,b.forwarderParser,");
		Hql.append(" b.datasourceName,b.config,b.configInfo");
		Hql.append(" FROM  ConfAgent a,ConfDatasource b ");
		Hql.append(" WHERE a.id = b.agentId");
		Hql.append(" GROUP BY b.createDate");
		
		pm = confDatasourceDao.query(Hql.toString(), offSet, pageSize);
		List<Object>dataList = pm.getList();
		if(dataList.size()!=0){
			for (int i = 0; i < dataList.size(); i++) {
				Object[] obj = (Object[]) dataList.get(i);
				confData = new ConfDatasourceVo();
				confData.setAgentName(obj[0].toString());
				confData.setId(obj[1].toString());
				confData.setAgentId(obj[2].toString());
				confData.setCategory(obj[3].toString());
				if(null != obj[4]){
					confData.setPattern(obj[4].toString());
				}
				confData.setAgentParser(obj[5].toString());
				confData.setEncoding(obj[6].toString());
				//confData.setForwarderName(obj[7].toString());
				confData.setCategoryId(obj[8].toString());
				if(null != obj[9]){
					confData.setHost(obj[9].toString());
				}
				confData.setState(obj[10].toString());
				confData.setType(obj[11].toString());
				if(null != obj[12]){
					confData.setUrl(obj[12].toString());
				}
				if(null!=obj[13]){
					confData.setPort(Long.parseLong(obj[13].toString()));
				}
				confData.setProtocol(obj[14].toString());
				confData.setForwarderParser(obj[15].toString());
				confData.setDatasourceName(obj[16].toString());
				confData.setConfig(obj[17].toString());
				if(null!=obj[18]){
					confData.setConfigInfo(obj[18].toString());
				}
				listDatasource.add(confData);
			}
			pm.setList(listDatasource);
			pm.setTotalCount(listDatasource.size());
			int totalPages = confDatasourceDao.totalPages(pageSize);
			for (int i = 1; i <= totalPages; i++) {
				pages.add(i);
			}
			pm.setCurrentPage(offSet);
			pm.setTotalPages(totalPages);
			pm.setPageNum(pages);
		}else{
			pm = confDatasourceDao.query(Hql.toString(), offSet-1, pageSize);
			List<Object>dataPageList = pm.getList();
			for (int i = 0; i < dataPageList.size(); i++) {
				Object[] obj = (Object[]) dataPageList.get(i);
				confData = new ConfDatasourceVo();
				confData.setAgentName(obj[0].toString());
				confData.setId(obj[1].toString());
				confData.setAgentId(obj[2].toString());
				confData.setCategory(obj[3].toString());
				if(null != obj[4]){
					confData.setPattern(obj[4].toString());
				}
				confData.setAgentParser(obj[5].toString());
				confData.setEncoding(obj[6].toString());
				//confData.setForwarderName(obj[7].toString());
				confData.setCategoryId(obj[8].toString());
				if(null != obj[9]){
					confData.setHost(obj[9].toString());
				}
				confData.setState(obj[10].toString());
				confData.setType(obj[11].toString());
				if(null != obj[12]){
					confData.setUrl(obj[12].toString());
				}
				if(null!=obj[13]){
					confData.setPort(Long.parseLong(obj[13].toString()));
				}
				confData.setProtocol(obj[14].toString());
				confData.setForwarderParser(obj[15].toString());
				confData.setDatasourceName(obj[16].toString());
				confData.setConfig(obj[17].toString());
				if(null!=obj[18]){
					confData.setConfigInfo(obj[18].toString());
				}
				listDatasource.add(confData);
			}
			pm.setList(listDatasource);
			pm.setTotalCount(listDatasource.size());
			int totalPages = confDatasourceDao.totalPages(pageSize);
			for (int i = 1; i <= totalPages; i++) {
				pages.add(i);
			}
			pm.setCurrentPage(offSet-1);
			pm.setTotalPages(totalPages);
			pm.setPageNum(pages);
		}
		
		return pm;
	}
	public void rollBack(Session session)throws Exception{
		confDatasourceDao.rollBack(session);
	}
	public Session dataSourceSession()throws Exception{
		return confDatasourceDao.session();
	}
	public boolean save(ConfDatasource confDatasource)throws Exception{
		Session session = confDatasourceDao.saveDataSource(confDatasource);
		boolean flag = Config.addAgent(confDatasource);
		if (flag) {
			if (session != null ) {
				if(session.isOpen()){
					session.close();
				}
			}
		} else {
			if(session != null){
				if(session.isOpen()){
					confDatasourceDao.rollBack(session);
				}
			}
		}
		return flag;
	}
	public boolean updateDataSource(ConfDatasource confDatasource)throws Exception{
		Session session = confDatasourceDao.updateDataSource(confDatasource);
		boolean flag = Config.updateAgent(confDatasource);
		if (flag) {
			if (session != null) {
				if(session.isOpen()){
					session.close();
				}
			}
		}else{
			if(session != null){
				if(session.isOpen()){
					confDatasourceDao.rollBack(session);
				}
			}
		}
		return flag;
	}
	public boolean deleteDataSource(ConfDatasource confDatasource)throws Exception{
		boolean flag = Config.deleteAgent(confDatasource);
		Session session = confDatasourceDao.deleteDataSource(confDatasource);
		if (flag) {
			if (session != null) {
				if(session.isOpen()){
					session.close();
				}
			}
		}else{
			if (session != null) {
				if(session.isOpen()){
					confDatasourceDao.rollBack(session);
				}
			}
		}
		return flag;
	}
}
