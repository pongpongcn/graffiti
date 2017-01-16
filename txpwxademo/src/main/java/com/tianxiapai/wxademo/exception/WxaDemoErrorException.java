package com.tianxiapai.wxademo.exception;

import com.tianxiapai.wxademo.model.WxaDemoError;

public class WxaDemoErrorException extends Exception {
	private static final long serialVersionUID = -4072683399809210002L;

	private WxaDemoError error;

	public WxaDemoErrorException(WxaDemoError error) {
		super(error.toString());
		this.error = error;
	}

	public WxaDemoErrorException(WxaDemoError error, Throwable cause) {
		super(error.toString(), cause);
		this.error = error;
	}

	public WxaDemoError getError() {
		return error;
	}

	public void setError(WxaDemoError error) {
		this.error = error;
	}
}
