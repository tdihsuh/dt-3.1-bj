package com.hansight.kunlun.coordinator.config;

import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MonitorForwarderConfigTest {
	private final static String FORWARDER_ID = "forwarder_001";
	private final static String TYPE_IIS = "iis";
	private final static String TYPE_APACHE = "apache";
	private final static int MAX_PER_OPERATION = 10;
	private final static int TOTAL_ACTION = MAX_PER_OPERATION * 3;
	private final static int TRIGGER_SLEEP_MS = 200;
	private List<String> configIdList = Lists.newArrayList();
	private MonitorService service = null;
	private AtomicInteger newCounter = new AtomicInteger(0);
	private AtomicInteger updateCounter = new AtomicInteger(0);
	private AtomicInteger deleteCounter = new AtomicInteger(0);

	@Before
	public void setUp() throws Exception {
		try {
			this.service = new ForwarderMonitorService(FORWARDER_ID);
			this.service.registerConfigChangedProcessor(new ConfigChangedAction(
                    newCounter, updateCounter, deleteCounter));
			for (int i = 0; i < MAX_PER_OPERATION; i++) {
				configIdList.add("config_" + i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
		this.service.close();
	}

	@Test
	public void testMonitor() throws InterruptedException {
		ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(this.service);
		es.submit(new GenerateData(FORWARDER_ID, configIdList));
		es.shutdown();
		es.awaitTermination(10, TimeUnit.SECONDS);
		System.out.println("new:" + newCounter.get()+",update:"+updateCounter.get()+",delete:"+deleteCounter.get());
		assertEquals(TOTAL_ACTION, (newCounter.get()+updateCounter.get()+deleteCounter.get()));
	}

	private static class GenerateData implements Runnable {
		private String forwarderId = null;
		private ForwarderConfigService configService = null;
		private List<String> configIdList = null;

		public GenerateData(String forwarderId, List<String> configIdList) {
			super();
			this.forwarderId = forwarderId;
			this.configIdList = configIdList;
			this.configService = new ForwarderConfigService(this.forwarderId);
		}

		@Override
		public void run() {
			try{
				// add
				for (String configId : this.configIdList) {
					ForwarderConfig config = new ForwarderConfig();
					config.put(ConfigConstants.DATASOURCE_ID, configId);
					config.put("category",TYPE_IIS);
					TimeUnit.MILLISECONDS.sleep(TRIGGER_SLEEP_MS);
					this.configService.add(config);
				}
				// update
				for (String configId : this.configIdList) {
                    ForwarderConfig config = new ForwarderConfig();
					config.put(ConfigConstants.DATASOURCE_ID, configId);
					config.put("category",TYPE_APACHE);
					TimeUnit.MILLISECONDS.sleep(TRIGGER_SLEEP_MS);
					this.configService.update(config);
				}
			// delete
				for (String configId : this.configIdList) {
                    ForwarderConfig config = new ForwarderConfig();
					config.put(ConfigConstants.DATASOURCE_ID, configId);
					TimeUnit.MILLISECONDS.sleep(TRIGGER_SLEEP_MS);
					this.configService.delete(config);
			}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	private static class ConfigChangedAction implements
			MonitorService.ConfigChangedProcessor<ForwarderConfig>{
		private AtomicInteger newCounter = null;
		private AtomicInteger updateCounter = null;
		private AtomicInteger deleteCounter = null;

		public ConfigChangedAction(AtomicInteger newCounter,
				AtomicInteger updateCounter, AtomicInteger deleteCounter) {
			super();
			this.newCounter = newCounter;
			this.updateCounter = updateCounter;
			this.deleteCounter = deleteCounter;
		}

		@Override
		public void process(List<ForwarderConfig> configList) throws MonitorException {
			System.out.println("Config List size: "+ configList.size());
			System.out.println("--- new:" + newCounter.get()+",update:"+updateCounter.get()+",delete:"+deleteCounter.get());
			for (Config config : configList) {
				System.out.println("inside configed processor: "+config);
				switch (config.getState()) {
				case NEW:
//					if(config.getType().endsWith(TYPE_IIS)){
						this.newCounter.incrementAndGet();
//					}
					break;
				case UPDATE:
//					if(config.getType().endsWith(TYPE_APACEH)){
						this.updateCounter.incrementAndGet();
//					}
					break;
				case DELETE:
					this.deleteCounter.incrementAndGet();
					break;
				}
			}
		}
	}
}
