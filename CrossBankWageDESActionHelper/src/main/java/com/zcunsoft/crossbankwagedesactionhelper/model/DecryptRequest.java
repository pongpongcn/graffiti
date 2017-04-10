package com.zcunsoft.crossbankwagedesactionhelper.model;

public class DecryptRequest {
	private String encryptedData;
	private String key;
	public String getEncryptedData() {
		return encryptedData;
	}
	public void setEncryptedData(String encryptedData) {
		this.encryptedData = encryptedData;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
}
