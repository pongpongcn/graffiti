package com.zcunsoft.weixin.api;

import com.zcunsoft.weixin.model.WxSession;

import me.chanjar.weixin.common.exception.WxErrorException;

public interface WxMpService extends me.chanjar.weixin.mp.api.WxMpService {
	WxSession getSessionByJsCode(String code) throws WxErrorException;
}
