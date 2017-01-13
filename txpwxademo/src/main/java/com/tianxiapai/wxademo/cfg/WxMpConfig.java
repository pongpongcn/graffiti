package com.tianxiapai.wxademo.cfg;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("wxmp")
public class WxMpConfig {
	/**
	 * 小程序ID
	 */
	private String appId;
	/**
	 * 小程序密钥
	 */
	private String appSecret;

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
}
