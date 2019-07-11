package com.yishuifengxiao.common.security.event;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证失败的事件
 * 
 * @author yishui
 * @Date 2019年5月30日
 * @version 1.0.0
 */
public class AuthenticationFailureEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1240683622233923579L;

	public AuthenticationFailureEvent(Object source, HttpServletRequest request) {
		super(source, request);
	}

}
