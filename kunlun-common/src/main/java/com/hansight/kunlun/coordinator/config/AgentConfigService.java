package com.hansight.kunlun.coordinator.config;




public class AgentConfigService extends ConfigServiceBase<AgentConfig> {

	/**
	 * Default constructor
	 * @param agentId, agent component id that construct znode path
	 */
	public AgentConfigService(String agentId){
		super();
		this.basePath = ConfigUtils.normalizationPath(ConfigConstants.AGENT_BASE_PATH_TEMPLATE,agentId);
	}
	
}
