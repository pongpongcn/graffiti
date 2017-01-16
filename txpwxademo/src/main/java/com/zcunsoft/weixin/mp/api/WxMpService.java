package com.zcunsoft.weixin.mp.api;

import com.zcunsoft.weixin.mp.model.result.WxSession;

import me.chanjar.weixin.common.exception.WxErrorException;

public interface WxMpService extends me.chanjar.weixin.mp.api.WxMpService {
	WxSession getSessionByJsCode(String code) throws WxErrorException;
}
