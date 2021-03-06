package com.zcunsoft.weixin.mp.model.result;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WxSession implements Serializable {
	private static final long serialVersionUID = -4267301003105863705L;

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
