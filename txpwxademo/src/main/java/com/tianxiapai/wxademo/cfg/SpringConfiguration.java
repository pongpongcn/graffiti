package com.tianxiapai.wxademo.cfg;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zcunsoft.weixin.mp.api.WxMpService;
import com.zcunsoft.weixin.mp.api.impl.WxMpServiceImpl;

import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;

@Configuration
@EnableConfigurationProperties({ WxMpConfig.class })
public class SpringConfiguration {
	private final WxMpConfig wxMpConfig;

	public SpringConfiguration(WxMpConfig wxMpConfig) {
		this.wxMpConfig = wxMpConfig;
	}

	@Bean
	public WxMpService wxMpService() {
		WxMpInMemoryConfigStorage config = new WxMpInMemoryConfigStorage();
		config.setAppId(wxMpConfig.getAppId());
		config.setSecret(wxMpConfig.getAppSecret());

		WxMpService wxService = new WxMpServiceImpl();
		wxService.setWxMpConfigStorage(config);

		return wxService;
	}
}
