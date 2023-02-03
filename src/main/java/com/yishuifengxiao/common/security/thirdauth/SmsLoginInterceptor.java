package com.yishuifengxiao.common.security.thirdauth;

import com.yishuifengxiao.common.security.httpsecurity.authorize.AbstractAuthorizeProvider;
import com.yishuifengxiao.common.security.thirdauth.sms.SmsAuthenticationFilter;
import com.yishuifengxiao.common.security.thirdauth.sms.SmsAuthenticationProvider;
import com.yishuifengxiao.common.security.thirdauth.sms.SmsUserDetailsService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * <p>短信登陆拦截器</p>

 * 将短信验证码的几个配置参数串联起来 将自定义的短信处理方式配置进spring security，使系统具备通过短信登陆的能力
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class SmsLoginInterceptor extends AbstractAuthorizeProvider {

	private AuthenticationSuccessHandler customAuthenticationFailureHandler;

	private AuthenticationFailureHandler customAuthenticationSuccessHandler;

	private SmsUserDetailsService smsUserDetailsService;
	/**
	 * 短信登录的URL
	 */
	private String url;

	@Override
	public void configure(HttpSecurity http) throws Exception {

		SmsAuthenticationFilter smsCodeAuthenticationFilter = new SmsAuthenticationFilter(this.url);
		smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(customAuthenticationFailureHandler);
		smsCodeAuthenticationFilter.setAuthenticationFailureHandler(customAuthenticationSuccessHandler);

		SmsAuthenticationProvider smsCodeAuthenticationProvider = new SmsAuthenticationProvider();
		smsCodeAuthenticationProvider.setSmsUserDetailsService(smsUserDetailsService);

		http.authenticationProvider(smsCodeAuthenticationProvider).addFilterAfter(smsCodeAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class);

	}

	public SmsLoginInterceptor(AuthenticationSuccessHandler customAuthenticationFailureHandler,
			AuthenticationFailureHandler customAuthenticationSuccessHandler,SmsUserDetailsService smsUserDetailsService,
			String url) {
		this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
		this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
		this.smsUserDetailsService = smsUserDetailsService;
		this.url = url;
	}

	public SmsLoginInterceptor() {

	}

}