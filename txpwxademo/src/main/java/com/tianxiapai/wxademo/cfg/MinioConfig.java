package com.tianxiapai.wxademo.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("minio")
public class MinioConfig {
	private String endpoint;
	private String accessKey;
	private String secretKey;

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}
