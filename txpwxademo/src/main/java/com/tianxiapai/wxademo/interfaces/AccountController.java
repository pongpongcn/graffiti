package com.tianxiapai.wxademo.interfaces;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tianxiapai.wxademo.exception.WxaDemoErrorException;
import com.tianxiapai.wxademo.model.WxaDemoError;
import com.zcunsoft.weixin.mp.api.WxMpService;
import com.zcunsoft.weixin.mp.model.result.WxSession;

import me.chanjar.weixin.common.exception.WxErrorException;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final WxMpService wxService;

	public AccountController(WxMpService wxService) {
		this.wxService = wxService;
	}

	@RequestMapping("/token")
	public Map<String, String> token(HttpSession session) {
		return Collections.singletonMap("token", session.getId());
	}

	@RequestMapping(value = "/login_wx", method = RequestMethod.POST)
	public void login(@RequestParam String code, HttpSession session) throws WxaDemoErrorException {
		if (logger.isDebugEnabled()) {
			logger.debug("Try to login by weixin js code: {}.", code);
		}

		session.setAttribute("currentTimeInMillis", Calendar.getInstance().getTimeInMillis());

		try {
			WxSession wxSession = wxService.getSessionByJsCode(code);
			session.setAttribute("wxSession", wxSession);
		} catch (WxErrorException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Execute getSessionByJsCode failed, js code: {}.", code);
			}

			WxaDemoError error = new WxaDemoError(345, "Get Session failed.");
			throw new WxaDemoErrorException(error, e);
		}
	}
}