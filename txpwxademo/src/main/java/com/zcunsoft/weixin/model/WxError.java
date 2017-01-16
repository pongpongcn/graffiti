package com.zcunsoft.weixin.model;

/**
 * 微信API错误信息
 * 
 * @author Pocketpc
 *
 */
public class WxError {
	private int errcode;
	private String errmsg;

	public int getErrcode() {
		return errcode;
	}

	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	@Override
	public String toString() {
		return "WxError{" + "errcode=" + errcode + ", errmsg=\"" + errmsg + "\"" + "}";
	}
}
