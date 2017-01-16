package com.tianxiapai.wxademo.interfaces;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcunsoft.weixin.api.model.WxError;
import com.zcunsoft.weixin.api.sns.result.WxaSession;

import me.chanjar.weixin.mp.api.WxMpService;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WxMpService wxService;

	public AccountController(WxMpService wxService) {
		this.wxService = wxService;
	}

	@RequestMapping(value = "/login_wx", method = RequestMethod.POST)
	public void login(@RequestParam String code) throws JsonParseException, JsonMappingException, IOException {
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={js_code}&grant_type={grant_type}";

		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("appid", wxService.getWxMpConfigStorage().getAppId());
		uriVariables.put("secret", wxService.getWxMpConfigStorage().getSecret());
		uriVariables.put("js_code", code);
		uriVariables.put("grant_type", "authorization_code");

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, uriVariables);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			if (logger.isDebugEnabled()) {
				logger.debug("Call jscode2session result: {}.", responseEntity.getBody());
			}

			ObjectMapper wxObjectMapper = new ObjectMapper();
			TypeReference<Map<String, String>> resultMapTypeReference = new TypeReference<Map<String, String>>() {
			};

			Map<String, String> resultMap = wxObjectMapper.readValue(responseEntity.getBody(), resultMapTypeReference);

			if (resultMap.containsKey("errcode")) {
				WxError wxError = wxObjectMapper.readValue(responseEntity.getBody(), WxError.class);
				if (logger.isDebugEnabled()) {
					logger.debug("Got WxError: {}.", wxError);
				}
			} else {
				WxaSession wxaSession = wxObjectMapper.readValue(responseEntity.getBody(), WxaSession.class);
				if (logger.isDebugEnabled()) {
					logger.debug("Got WxaSession: {}.", wxaSession);
				}
			}
		}
	}
}