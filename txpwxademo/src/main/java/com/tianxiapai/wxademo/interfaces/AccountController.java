package com.tianxiapai.wxademo.interfaces;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
	
	@RequestMapping(value = "/ping", method = RequestMethod.GET)
	public Map<String, String> ping(HttpSession session){
		Map<String,String> sa = new HashMap<>();
		sa.put("sessionId", session.getId());
		return sa;
	}
	
	@RequestMapping(value = "/login_wx", method = RequestMethod.POST)
	public void login(@RequestParam String code, HttpSession session) throws WxaDemoErrorException {
		if (logger.isDebugEnabled()) {
			logger.debug("Try to login by weixin js code: {}.", code);
		}

		try {
			WxSession wxSession = wxService.getSessionByJsCode(code);
			session.setAttribute("wxSession", wxSession);
			
			Authentication authentication = new UsernamePasswordAuthenticationToken("WeixinUser_" + code, null, AuthorityUtils.NO_AUTHORITIES);
	        SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (WxErrorException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Execute getSessionByJsCode failed, js code: {}.", code);
			}

			WxaDemoError error = new WxaDemoError(345, "Get Session failed.");
			throw new WxaDemoErrorException(error, e);
		}
	}
	
	@RequestMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void logout(HttpSession session) {
		session.invalidate();
		
		if (logger.isDebugEnabled()) {
			logger.debug("Session has been invalidate, session id: {}.", session.getId());
		}
	}
}