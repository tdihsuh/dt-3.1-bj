package com.hansight.kunlun.coordinator.config;

import com.google.common.collect.Lists;
import com.hansight.kunlun.coordinator.metric.ConfigInfo;
import com.hansight.kunlun.coordinator.metric.LiveInfo;
import com.hansight.kunlun.coordinator.metric.MetricMsg;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.ProcessorType;
import com.hansight.kunlun.coordinator.metric.WorkerStatus.ConfigStatus;

import junit.framework.TestCase;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ForwarderConfigServiceTest extends TestCase{
	private final static String FORWARDER_ID = "forwarder_001";
	private List<String> configIdList = Lists.newArrayList();
	private ConfigService service = null;
	private TestingServer server = null;

	@Before
	public void setUp() throws Exception {
		try{
			this.server = new TestingServer();
			CuratorFramework client = null;
			client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
			this.service = new ForwarderConfigService(FORWARDER_ID);
//			this.service.setClient(client);
			for(int i = 0; i < 10; i++){
				configIdList.add("config_"+i);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		this.service.close();
		CloseableUtils.closeQuietly(this.server);
	}

	@Test
	public void testAdd() throws ConfigException,ConnectException {
		for(String configId:this.configIdList){
			Config config = new Config();
			config.put(ConfigConstants.DATASOURCE_ID, configId);
			config.put("category","iis");
			this.service.add(config);
		}
		List<Config> configList = this.service.queryAll();
		for(Config config:configList){
			assertTrue(true == configIdList.contains(config.get(ConfigConstants.DATASOURCE_ID)));
			assertTrue(config.getState().equals(Config.State.NEW));
		}
	}

	@Test
	public void testUpdate() throws ConfigException,ConnectException {
		testAdd();
		List<Config> configList = this.service.queryAll();
		for(Config config:configList){
			config.put("category", "apaceh");
			this.service.update(config);
		}
		configList = this.service.queryAll();
		for(Config config:configList){
			assertTrue(true == config.get("category").equals("apache"));
			assertTrue(config.getState().equals(Config.State.UPDATE));
		}
	}

	@Test
	public void testDelete() throws ConfigException,ConnectException {
		testAdd();
		for(String configId:this.configIdList){
			Config config = new Config();
			config.put(ConfigConstants.DATASOURCE_ID, configId);
			this.service.delete(config);
		}
		List<Config> configList = this.service.queryAll();
		assertEquals(0, configList.size());
	}

	@Test
	public void testQueryAll() throws ConfigException,ConnectException {
		//Step1 zk @init state
		List<Config> configList = this.service.queryAll();
		// result should 0 items
		assertEquals(0, configList.size());
		
		//Step2 zk @normal state, means that has children under basePath
		testAdd();
		configList = this.service.queryAll();
		assertEquals(configIdList.size(), configList.size());
	}

	@Test
	public void testDeleleById() throws Exception{
		String id = FORWARDER_ID;
		List<String> list = new ArrayList<String>();
		list.add(id);
		ConfigUtils.deleteAllByIds(list, ConfigCategory.FORWARDER);
	}
	
	@Test
	public void testDeletes() throws Exception{
		ForwarderConfig fc = new ForwarderConfig();
		fc.put(ConfigConstants.DATASOURCE_ID, "datasource_0");
		this.service.delete(fc);
		
		/*fc = new ForwarderConfig();
		fc.put(ConfigConstants.DATASOURCE_ID, "datasource_1");
		this.service.delete(fc);*/
	}
	
	@Test
	public void testAdds() throws Exception{
		List<String> agent = Lists.newArrayList();
		for(int i = 0; i < 2; i++){
			agent.add("datasource_"+i);
		}
		for(String ds : agent){
			ForwarderConfig fc = new ForwarderConfig();
			fc.put(ConfigConstants.DATASOURCE_ID, ds);
			this.service.add(fc);
		}
	}
	
	@Test
	public void testMetricBase() throws Exception {
		String datasourceId = "datasource_0";
		String processorId = "forwarder_001";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		ms.mark();
		
		/*String datasourceId = "datasource_1";
		String processorId = "forwarder_001";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		ms.mark();*/
	}
	
	@Test
	public void test2() throws Exception {
		String ds_uuid = ""+UUID.randomUUID().getLeastSignificantBits();
		ForwarderConfig forwarder = new ForwarderConfig();
        forwarder.put(ConfigConstants.DATASOURCE_ID, ds_uuid);
        forwarder.put("parser", "regex");
        forwarder.put("type", "log");
        forwarder.put("category", "IIS");
        forwarder.put("pattern", "%{IIS_LOG}");
        this.service.add(forwarder);
	}
	
	@Test
	public void testConfigStatus() throws Exception {
		MetricService ms = new MetricService();
		List<ConfigInfo> configStatus = ms.getConfigStatus();
		System.out.println(configStatus.size());
	}
	
	@Test
	public void testGetDatasourceId() throws Exception {
		MetricService ms = new MetricService();
		List<MetricMsg> list = ms.getDatasourceStatus();
		System.out.println(list.size());
	}
	
	@Test
	public void testGetLive() throws Exception{
		MetricService metric = new MetricService();
		List<LiveInfo> lives = metric.getLives(ProcessorType.FORWARDER);
		System.out.println(lives.size());
	}
}
