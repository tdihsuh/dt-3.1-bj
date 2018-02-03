package com.hansight.kunlun.coordinator.config;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.hansight.kunlun.coordinator.metric.ConfigInfo;
import com.hansight.kunlun.coordinator.metric.LiveInfo;
import com.hansight.kunlun.coordinator.metric.MetricException;
import com.hansight.kunlun.coordinator.metric.MetricMsg;
import com.hansight.kunlun.coordinator.metric.MetricService;
import com.hansight.kunlun.coordinator.metric.ProcessorType;
import com.hansight.kunlun.coordinator.metric.WorkerStatus.ConfigStatus;
import com.hansight.kunlun.utils.Common;

public class AgentConfigServiceTest extends TestCase{
	private final static String AGENT_ID = "agent_001";
	private List<String> configIdList = Lists.newArrayList();
	private ConfigService service = null;
	private TestingServer server = null;
	private CuratorFramework client = null;
	
	@Before
	public void setUp() throws Exception {
		try{
			this.server = new TestingServer();
			client = CuratorFrameworkFactory.newClient(server.getConnectString(), new ExponentialBackoffRetry(1000, 3));
			this.service = new AgentConfigService(AGENT_ID);
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
			config.put("category", "iis");
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
			config.put("category", "apache");
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
			config.put(ConfigConstants.DATASOURCE_ID,configId);
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
	public void testAllConfig() throws Exception{
		List<Config> configList = this.service.queryAll();
		for(Config config : configList){
			System.out.println(config.get("category") +"   " + config.get(ConfigConstants.DATASOURCE_ID));
		}
	}
	
	@Test
	public void testDeleteConfig() throws Exception{
		String path = "agent_001";
		List<String> configId = new ArrayList<String>();
		configId.add(path);
		ConfigUtils.deleteAllByIds(configId, ConfigCategory.AGENT);
	}

	@Test
	public void testChildrenPath() throws Exception{
		String path = "/kunlun/config/agent/agent_001";
		List<String> list = this.client.getChildren().forPath(path);
		for(String str:list){
			System.out.println(str);
		}
	}
	
	@Test
	public void testDeletes() throws Exception{
		AgentConfig agentConfig = new AgentConfig();
		agentConfig.put(ConfigConstants.DATASOURCE_ID, "datasource_0");
		this.service.delete(agentConfig);
		
		/*agentConfig = new AgentConfig();
		agentConfig.put(ConfigConstants.DATASOURCE_ID, "datasource_1");
		this.service.delete(agentConfig);*/
	}
	
	@Test
	public void testAdds() throws Exception{
		List<String> agent = Lists.newArrayList();
		for(int i = 0; i < 2; i++){
			agent.add("datasource_"+i);
		}
		for(String ds : agent){
			AgentConfig ac = new AgentConfig();
			ac.put(ConfigConstants.DATASOURCE_ID, ds);
			this.service.add(ac);
		}
	}
	
	@SuppressWarnings({ "resource", "unused" })
	@Test
	public void testAdd1() throws Exception {
		AgentConfig agent = new AgentConfig();
		agent.put(ConfigConstants.DATASOURCE_ID, "datasource_001");
		ForwarderConfig forward = new ForwarderConfig();
		forward.put(ConfigConstants.DATASOURCE_ID, "datasource_001");
		AgentConfigService agentConfigService = new AgentConfigService("agent_001");
		ForwarderConfigService forwarderConfigService = new ForwarderConfigService("forward_001");
		agentConfigService.add(agent);
		forwarderConfigService.add(forward);
		
		MetricService ms = new MetricService("datasource_001", "agent_001", ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		MetricService ms1 = new MetricService("datasource_001", "forward_001", ProcessorType.FORWARDER);
		ms1.setProcessorStatus(ConfigStatus.SUCCESS);
		
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		client = ConfigUtils.getClient(Common.get(Common.ZOOKEEPER_CONNECT, "localhost:2181"),
               Integer.valueOf(Common.get(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, "30000")) ,
                Integer.valueOf( Common.get(Common.ZOOKEEPER_CONNECTION_TIMEOUT,"60000")), retryPolicy);
		client.delete().deletingChildrenIfNeeded().forPath("/kunlun/metric/agent");
		
		
		MetricService mmmm = new MetricService();
		List<LiveInfo> status2 = mmmm.getStatus(ProcessorType.AGENT);
		List<LiveInfo> status3 = mmmm.getStatus(ProcessorType.FORWARDER);
		
		List<LiveInfo> status = ms.getStatus(ProcessorType.AGENT);
		Thread.sleep(1200000);
	}
	
	/**
	 * one agent, many forward test
	 * @throws Exception
	 */
	@Test
	public void testAdd2() throws Exception {
		AgentConfig agent = new AgentConfig();
		agent.put(ConfigConstants.DATASOURCE_ID, "datasource_001");
		ForwarderConfig forward = new ForwarderConfig();
		forward.put(ConfigConstants.DATASOURCE_ID, "datasource_001");
		AgentConfigService agentConfigService = new AgentConfigService("agent_001");
		ForwarderConfigService forwarderConfigService = new ForwarderConfigService("forward_001");
		agentConfigService.add(agent);
		forwarderConfigService.add(forward);
		
		forwarderConfigService = new ForwarderConfigService("forward_002");
		forwarderConfigService.add(forward);
		
		forwarderConfigService = new ForwarderConfigService("forward_003");
		forwarderConfigService.add(forward);
		
		MetricService ms = new MetricService("datasource_001", "agent_001", ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		MetricService ms1 = new MetricService("datasource_001", "forward_001", ProcessorType.FORWARDER);
		ms1.setProcessorStatus(ConfigStatus.SUCCESS);
		MetricService ms2 = new MetricService("datasource_001", "forward_002", ProcessorType.FORWARDER);
		ms2.setProcessorStatus(ConfigStatus.FAIL);
		MetricService ms3 = new MetricService("datasource_001", "forward_003", ProcessorType.FORWARDER);
		ms3.setProcessorStatus(ConfigStatus.FAIL);
		
	}
	
	@Test
	public void testssss() throws Exception {
		MetricService service = new MetricService();
		List<MetricMsg> datasourceStatus = service.getDatasourceStatus();
		List<ConfigInfo> configStatus = service.getConfigStatus();
		List<LiveInfo> lives = service.getLives(ProcessorType.AGENT);
	}
	
	@Test
	public void test1() throws Exception{
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		client = ConfigUtils.getClient(Common.get(Common.ZOOKEEPER_CONNECT, "localhost:2181"),
               Integer.valueOf(Common.get(Common.ZOOKEEPER_SESSION_TIMEOUT_MS, "30000")) ,
                Integer.valueOf( Common.get(Common.ZOOKEEPER_CONNECTION_TIMEOUT,"60000")), retryPolicy);
		
		String ds_uuid = ""+UUID.randomUUID().getLeastSignificantBits();
        AgentConfig agent = new AgentConfig();
        agent.put(ConfigConstants.DATASOURCE_ID, ds_uuid);
        agent.put("category", "default");
        agent.put("uri", "F:\\workspace\\kunlun\\kunlun-logger\\data\\ex131216.log");
        agent.put("start_position", "end");
        byte[] data = SerializationUtils.serialize(agent);
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/kunlun/metric/ds/"+ ds_uuid + "/agent_001" , data);
        //client.close();
        
        Thread.sleep(120000);
	}
	
	@Test
	public void testMetricBase() throws Exception {
		String datasourceId = "datasource_0";
		String processorId = "agent_001";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		ms.mark();
		
		datasourceId = "datasource_1";
		ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.FAIL);
		ms.mark();
		
		List<ConfigInfo> configStatus = ms.getConfigStatus();
		System.out.println(configStatus);
		/*String datasourceId = "datasource_0";
		String processorId = "agent_001";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.SUCCESS);
		ms.mark();*/
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
		MetricService ms = new MetricService();
		List<LiveInfo> lives = ms.getLives(ProcessorType.AGENT);
		System.out.println(lives.size());
	}
	
	@Test
	public void test1s()throws Exception{
		String datasourceId="2c905b5c48f462e30148f5962a63000b";
		String processorId="2c905b5c48f462e30148f594efd50009";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.FORWARDER);
		ms.setProcessorStatus(ConfigStatus.FAIL);
	}
	
	@Test
	public void test11s() throws Exception{
		String datasourceId="2c905b5c48f462e30148f5962a63000b";
		String processorId="2c905b5c48f462e30148f594ac560007";
		MetricService ms = new MetricService(datasourceId, processorId, ProcessorType.AGENT);
		ms.setProcessorStatus(ConfigStatus.FAIL);
	}
	
	@Test
	public void test2s() throws Exception{
		MetricService ms = new MetricService();
		Map<String, ConfigStatus> datasourceConfig = ms.getDatasourceConfig();
		System.out.println(datasourceConfig);
	}
	
	@Test
	public void test3s() throws Exception{
		AgentConfig agent = new AgentConfig();
		agent.put(ConfigConstants.DATASOURCE_ID, "datasource_001");
		
		AgentConfigService agentConfigService = new AgentConfigService("agent_001");
		agentConfigService.add(agent);
	}
	
	@SuppressWarnings("unused")
	@Test
	public void test4s() throws Exception{
		//MetricService ms = new MetricService("1a16c4e4-81ff-43c7-b6a0-734248eb6393", "2c905b5c49ae68a20149ae6a25730002", ProcessorType.AGENT);
		//ms.setProcessorStatus(ConfigStatus.SUCCESS);
		MetricService ms = new MetricService();
		List<ConfigInfo> configStatus = ms.getConfigStatus();
		Map<String, ConfigStatus> datasourceConfig = ms.getDatasourceConfig();
		List<LiveInfo> status = ms.getStatus(ProcessorType.AGENT);
		List<MetricMsg> datasourceStatus = ms.getDatasourceStatus();
	}
	
}
