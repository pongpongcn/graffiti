package com.tianxiapai.wxademo.security;

import java.util.Calendar;
import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class WeixinUserAuthentication extends AbstractAuthenticationToken{
	private static final long serialVersionUID = 1L;
	
	private final String principal;
	
	public WeixinUserAuthentication(Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		this.principal = "WeixinUser-" + Calendar.getInstance().getTimeInMillis();
		this.setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getPrincipal() {
		return this.principal;
	}
}