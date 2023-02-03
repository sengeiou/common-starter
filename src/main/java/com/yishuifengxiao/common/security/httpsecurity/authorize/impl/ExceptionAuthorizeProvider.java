package com.yishuifengxiao.common.security.httpsecurity.authorize.impl;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.yishuifengxiao.common.security.httpsecurity.AuthorizeProvider;
import com.yishuifengxiao.common.security.support.PropertyResource;

/**
 * 异常处理器
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ExceptionAuthorizeProvider implements AuthorizeProvider {

    private AuthenticationEntryPoint exceptionAuthenticationEntryPoint;

    private AccessDeniedHandler customAccessDeniedHandler;

    @Override
    public  void apply(PropertyResource propertyResource, HttpSecurity http) throws Exception {
        //@formatter:off
        http.exceptionHandling()
		// 定义的不存在access_token时候响应
		.authenticationEntryPoint(exceptionAuthenticationEntryPoint)
		//自定义权限拒绝处理器
		;
		//@formatter:on  
    }

    @Override
    public int order() {
        return 1000;
    }

    public AuthenticationEntryPoint getExceptionAuthenticationEntryPoint() {
        return exceptionAuthenticationEntryPoint;
    }

    public void setExceptionAuthenticationEntryPoint(AuthenticationEntryPoint exceptionAuthenticationEntryPoint) {
        this.exceptionAuthenticationEntryPoint = exceptionAuthenticationEntryPoint;
    }

    public AccessDeniedHandler getCustomAccessDeniedHandler() {
        return customAccessDeniedHandler;
    }

    public void setCustomAccessDeniedHandler(AccessDeniedHandler customAccessDeniedHandler) {
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    public ExceptionAuthorizeProvider(AuthenticationEntryPoint exceptionAuthenticationEntryPoint, AccessDeniedHandler customAccessDeniedHandler) {
        this.exceptionAuthenticationEntryPoint = exceptionAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    public ExceptionAuthorizeProvider() {

    }

}