package com.zcunsoft.crossbankwagedesactionhelper.model;

import javax.validation.constraints.Size;

public class EncryptRequest {
	private String data;
	private String key;
	@Size(min=1,max=1)
	private String padding;
	private boolean paddingLeft;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getPadding() {
		return padding;
	}

	public void setPadding(String padding) {
		this.padding = padding;
	}

	public boolean isPaddingLeft() {
		return paddingLeft;
	}

	public void setPaddingLeft(boolean paddingLeft) {
		this.paddingLeft = paddingLeft;
	}
}