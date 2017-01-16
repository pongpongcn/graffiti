package com.zcunsoft.weixin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WxSession {
	@JsonProperty("session_key")
	private String sessionKey;
	@JsonProperty("expires_in")
	private int expiresIn;
	private String openid;

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	@Override
	public String toString() {
		return "WxaSession{" + "sessionKey=\"" + sessionKey + "\", expiresIn=" + expiresIn + ", openid=\"" + openid
				+ "\"" + "}";
	}
}
