/**
 * 
 */
package com.yishuifengxiao.common.security.endpoint;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import com.yishuifengxiao.common.properties.SecurityProperties;
import com.yishuifengxiao.common.security.eunm.HandleEnum;
import com.yishuifengxiao.common.security.event.ExceptionAuthenticationEntryPointEvent;
import com.yishuifengxiao.common.security.processor.HandlerProcessor;
import com.yishuifengxiao.common.tool.entity.Response;
import com.yishuifengxiao.common.utils.HttpUtil;

/**
 * 当参数中不存在token时的提示信息 处理器<br/>
 * 参见 https://www.cnblogs.com/mxmbk/p/9782409.html
 * 
 * @author yishui
 * @Date 2019年4月2日
 * @version 1.0.0
 */
public class ExceptionAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

	private final static Logger log = LoggerFactory.getLogger(ExceptionAuthenticationEntryPoint.class);

	/**
	 * 声明了缓存与恢复操作
	 */
	private RequestCache cache = new HttpSessionRequestCache();

	private SecurityProperties securityProperties;

	/**
	 * 协助处理器
	 */
	private HandlerProcessor customHandle;

	private ApplicationContext context;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		// 引起跳转的url
		String url = cache.getRequest(request, response).getRedirectUrl();
		// 发布信息
		context.publishEvent(new ExceptionAuthenticationEntryPointEvent(authException, request));

		// 获取系统的处理方式
		HandleEnum handleEnum = securityProperties.getHandler().getException().getReturnType();

		HandleEnum type = HttpUtil.handleType(request, securityProperties.getHandler(), handleEnum);
		log.debug("【资源服务】获取资源 {} 失败(可能是缺少token) , 失败的原因为 {} , 系统配置的处理方式为 {} ,实际的处理方式为 {}", url,
				authException.getMessage(), handleEnum, type);

		if (type == HandleEnum.DEFAULT) {
			super.commence(request, response, authException);
			return;
		}

		customHandle.handle(request, response, type == HandleEnum.REDIRECT,
				securityProperties.getHandler().getException().getRedirectUrl(),
				new Response<>(Response.Const.CODE_UNAUTHORIZED, Response.Const.MSG_UNAUTHORIZED, authException));

	}

	public SecurityProperties getSecurityProperties() {
		return securityProperties;
	}

	public void setSecurityProperties(SecurityProperties securityProperties) {
		this.securityProperties = securityProperties;
	}

	public HandlerProcessor getCustomHandle() {
		return customHandle;
	}

	public void setCustomHandle(HandlerProcessor customHandle) {
		this.customHandle = customHandle;
	}

	public ExceptionAuthenticationEntryPoint(SecurityProperties securityProperties, HandlerProcessor customHandle) {
		this.securityProperties = securityProperties;
		this.customHandle = customHandle;
	}

	public ExceptionAuthenticationEntryPoint() {

	}

	public ApplicationContext getContext() {
		return context;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	public ExceptionAuthenticationEntryPoint(SecurityProperties securityProperties, HandlerProcessor customHandle,
			ApplicationContext context) {

		this.securityProperties = securityProperties;
		this.customHandle = customHandle;
		this.context = context;
	}

}
