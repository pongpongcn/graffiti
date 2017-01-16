package com.zcunsoft.weixin.api.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcunsoft.weixin.api.WxMpService;
import com.zcunsoft.weixin.model.WxError;
import com.zcunsoft.weixin.model.WxSession;

import me.chanjar.weixin.common.exception.WxErrorException;

public class WxMpServiceImpl extends me.chanjar.weixin.mp.api.impl.WxMpServiceImpl implements WxMpService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final ObjectMapper wxObjectMapper = new ObjectMapper();
	private final TypeReference<Map<String, String>> resultMapTypeReference = new TypeReference<Map<String, String>>() {
	};

	@Override
	public WxSession getSessionByJsCode(String code) throws WxErrorException {
		String url = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={js_code}&grant_type={grant_type}";

		Map<String, String> uriVariables = new HashMap<>();
		uriVariables.put("appid", this.getWxMpConfigStorage().getAppId());
		uriVariables.put("secret", this.getWxMpConfigStorage().getSecret());
		uriVariables.put("js_code", code);
		uriVariables.put("grant_type", "authorization_code");

		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class, uriVariables);

		if (responseEntity.getStatusCode() == HttpStatus.OK) {
			if (logger.isDebugEnabled()) {
				logger.debug("Called jscode2session url: {}, result: {}.", url, responseEntity.getBody());
			}

			Map<String, String> resultMap;
			try {
				resultMap = wxObjectMapper.readValue(responseEntity.getBody(), resultMapTypeReference);
			} catch (IOException e) {
				// 这种情况是非预期的。
				throw new RuntimeException("Failed to read value to Map, value: " + responseEntity.getBody() + ".", e);
			}

			if (!resultMap.containsKey("errcode")) {
				WxSession wxaSession;
				try {
					wxaSession = wxObjectMapper.readValue(responseEntity.getBody(), WxSession.class);
				} catch (IOException e) {
					// 这种情况是非预期的。
					throw new RuntimeException(
							"Failed to parse value to WxaSession, value: " + responseEntity.getBody() + ".", e);
				}

				if (logger.isDebugEnabled()) {
					logger.debug("Got WxaSession: {}.", wxaSession);
				}

				return wxaSession;
			} else {
				WxError wxError;
				try {
					wxError = wxObjectMapper.readValue(responseEntity.getBody(), WxError.class);
					if (logger.isDebugEnabled()) {
						logger.debug("Got WxError: {}.", wxError);
					}
				} catch (IOException e) {
					// 这种情况是非预期的。
					throw new RuntimeException(
							"Failed to parse value to WxError, value: " + responseEntity.getBody() + ".", e);
				}

				me.chanjar.weixin.common.bean.result.WxError wxErrorOutput = new me.chanjar.weixin.common.bean.result.WxError();
				wxErrorOutput.setErrorCode(wxError.getErrcode());
				wxErrorOutput.setErrorMsg(wxError.getErrmsg());

				throw new WxErrorException(wxErrorOutput);
			}
		} else {
			// 这种情况是非预期的。
			throw new RuntimeException("Weixin API return unexpected statusCode: " + responseEntity.getStatusCode());
		}
	}

}
