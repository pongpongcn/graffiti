package com.tianxiapai.wxademo.interfaces.dto;

import javax.validation.constraints.NotNull;

public class LoginWxRequest {
	@NotNull
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
