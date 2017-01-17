package com.tianxiapai.wxademo.interfaces;

import java.security.Principal;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tianxiapai.wxademo.exception.WxaDemoErrorException;
import com.tianxiapai.wxademo.interfaces.dto.LoginWxRequest;
import com.tianxiapai.wxademo.interfaces.dto.LoginWxResponse;
import com.tianxiapai.wxademo.model.WxaDemoError;
import com.tianxiapai.wxademo.security.WeixinUserAuthentication;
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
	public String ping(Principal user) {
		return "Pong, " + user.getName() + ".";
	}

	@RequestMapping(value = "/login_wx", method = RequestMethod.POST)
	public LoginWxResponse login(@RequestBody LoginWxRequest request, HttpSession session)
			throws WxaDemoErrorException {
		String code = request.getCode();

		if (logger.isDebugEnabled()) {
			logger.debug("Try to login by weixin js code: {}.", code);
		}

		try {
			WxSession wxSession = wxService.getSessionByJsCode(code);
			session.setAttribute("wxSession", wxSession);

			// makes WxSession bind to user.
			Authentication authentication = new WeixinUserAuthentication(AuthorityUtils.NO_AUTHORITIES);
			SecurityContextHolder.getContext().setAuthentication(authentication);

			LoginWxResponse response = new LoginWxResponse();
			response.setSessionId(session.getId());
			return response;
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