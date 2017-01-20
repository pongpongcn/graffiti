package com.tianxiapai.wxademo.interfaces.dto;

import javax.validation.constraints.NotNull;

public class LoginWxResponse {
	@NotNull
	private String sessionId;

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
