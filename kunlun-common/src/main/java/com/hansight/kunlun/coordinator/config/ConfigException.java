package com.hansight.kunlun.coordinator.config;

public class ConfigException extends Exception {
	private static final long serialVersionUID = 4497383690317486086L;

	public ConfigException(String message) {
		super(message);
	}

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}
}
