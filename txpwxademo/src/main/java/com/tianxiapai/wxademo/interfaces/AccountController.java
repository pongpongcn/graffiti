package com.tianxiapai.wxademo.interfaces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tianxiapai.wxademo.exception.WxaDemoErrorException;
import com.tianxiapai.wxademo.model.WxaDemoError;
import com.zcunsoft.weixin.mp.api.WxMpService;

import me.chanjar.weixin.common.exception.WxErrorException;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WxMpService wxService;

	public AccountController(WxMpService wxService) {
		this.wxService = wxService;
	}

	@RequestMapping(value = "/login_wx", method = RequestMethod.POST)
	public void login(@RequestParam String code) throws WxaDemoErrorException {
		if (logger.isDebugEnabled()) {
			logger.debug("Try to login by weixin js code: {}.", code);
		}

		try {
			wxService.getSessionByJsCode(code);
		} catch (WxErrorException e) {
			if (logger.isInfoEnabled()) {
				logger.info("Execute getSessionByJsCode failed, js code: {}.", code);
			}
			
			WxaDemoError error = new WxaDemoError(345, "Get Session failed.");
			throw new WxaDemoErrorException(error, e);
		}
	}
}