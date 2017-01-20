package com.tianxiapai.wxademo.cfg;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.zcunsoft.weixin.mp.api.WxMpService;
import com.zcunsoft.weixin.mp.api.impl.WxMpServiceImpl;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;
import me.chanjar.weixin.mp.api.WxMpInMemoryConfigStorage;

@Configuration
@EnableConfigurationProperties({ WxMpConfig.class, MinioConfig.class })
public class SpringConfiguration {
	private final WxMpConfig wxMpConfig;
	private final MinioConfig minioConfig;

	public SpringConfiguration(WxMpConfig wxMpConfig, MinioConfig minioConfig) {
		this.wxMpConfig = wxMpConfig;
		this.minioConfig = minioConfig;
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

	@Bean
	public MinioClient minioClient() {
		try {
			return new MinioClient(minioConfig.getEndpoint(), minioConfig.getAccessKey(), minioConfig.getSecretKey());
		} catch (InvalidEndpointException | InvalidPortException e) {
			throw new RuntimeException("Init minioClient failed", e);
		}
	}
}
