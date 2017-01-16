package com.tianxiapai.wxademo.model;

public class WxaDemoError {
	public final int code;
    public final String message;
    
    public WxaDemoError(int code, String message) {
        this.code = code;
        this.message = message;
    }

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return "WxaDemoError{" + "code=" + code + ", message=\"" + message + "\"" + "}";
	}
}
