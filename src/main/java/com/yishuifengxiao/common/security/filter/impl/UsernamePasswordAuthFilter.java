package com.yishuifengxiao.common.security.filter.impl;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.yishuifengxiao.common.security.extractor.SecurityExtractor;
import com.yishuifengxiao.common.security.filter.SecurityRequestFilter;
import com.yishuifengxiao.common.security.processor.HandlerProcessor;
import com.yishuifengxiao.common.security.resource.PropertyResource;
import com.yishuifengxiao.common.security.support.SecurityHelper;
import com.yishuifengxiao.common.security.token.SecurityToken;
import com.yishuifengxiao.common.tool.exception.CustomException;

/**
 * <p>
 * 登陆时用户名和密码校验
 * </p>
 * 
 * 即在系统默认校验之前检查一下用户名和密码是否正确 ,
 * 
 * 用于在UsernamePasswordAuthenticationFilter
 * 之前提前校验一下用户名是否已经存在,会在UsernameAuthInterceptor中被收集注入
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class UsernamePasswordAuthFilter extends SecurityRequestFilter {

	private AntPathRequestMatcher pathMatcher = null;

	private HandlerProcessor handlerProcessor;

	private SecurityHelper securityHelper;

	private PropertyResource propertyResource;

	/**
	 * 信息提取器
	 */
	private SecurityExtractor securityExtractor;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 是否关闭前置参数校验功能
		Boolean closePreAuth = propertyResource.security().getCore().getClosePreAuth();
		if (BooleanUtils.isNotTrue(closePreAuth)) {
			AntPathRequestMatcher pathMatcher = this.antPathMatcher();
			if (pathMatcher.matches(request)) {

				String username = securityExtractor.extractUsername(request, response);
				String password = securityExtractor.extractPassword(request, response);

				if (username == null) {
					username = "";
				}
				if (password == null) {
					password = "";
				}

				username = username.trim();

				try {
					// 生成token
					String sessionId = securityExtractor.extractUserUniqueIdentitier(request, response);
					SecurityToken token = securityHelper.create(username, password, sessionId);
					// 获取认证信息
					Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
					handlerProcessor.login(request, response, authentication, token);
					return;
				} catch (CustomException exception) {
					handlerProcessor.failure(propertyResource, request, response, exception);
					return;
				} catch (Exception e) {
					handlerProcessor.exception(propertyResource, request, response, e);
					return;
				}

			}
		}

		filterChain.doFilter(request, response);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore(this, UsernamePasswordAuthenticationFilter.class);

	}

	/**
	 * 获取到路径匹配器
	 * 
	 * @return 路径匹配器
	 */
	private AntPathRequestMatcher antPathMatcher() {
		if (null == this.pathMatcher) {
			this.pathMatcher = new AntPathRequestMatcher(this.propertyResource.security().getCore().getFormActionUrl());
		}
		return this.pathMatcher;
	}

	public UsernamePasswordAuthFilter(HandlerProcessor handlerProcessor, SecurityHelper securityHelper,
			PropertyResource propertyResource, SecurityExtractor securityExtractor) {
		this.handlerProcessor = handlerProcessor;
		this.securityHelper = securityHelper;
		this.propertyResource = propertyResource;
		this.securityExtractor = securityExtractor;
	}

}
