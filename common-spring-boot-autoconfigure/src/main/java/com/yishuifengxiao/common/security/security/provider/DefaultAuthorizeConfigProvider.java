/**
 * 
 */
package com.yishuifengxiao.common.security.security.provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

import com.yishuifengxiao.common.properties.SecurityProperties;

/**
 * AuthorizeConfigProvider的默认配置
 * 
 * @author yishui
 * @date 2019年1月8日
 * @version 0.0.1
 */
@Component
public class DefaultAuthorizeConfigProvider implements AuthorizeConfigProvider {
	/**
	 * 自定义属性配置
	 */
	@Autowired
	protected SecurityProperties securityProperties;
	

	@Override
	public void config(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry config) {
		// @formatter:off
		config.antMatchers("/oauth/token", securityProperties.getCore().getRedirectUrl(), // 权限拦截时默认的跳转地址
				securityProperties.getCore().getLoginPage(), // 登陆页面的URL
				securityProperties.getCore().getFormActionUrl(), // 登陆页面表单提交地址
				securityProperties.getCore().getLoginOutUrl()).permitAll()// 登出页面的地址
				// .antMatchers("/js/**","/css/**","/images/**","/fonts/**","/**/**.png","/**/**.jpg","/**/**.html","/**/**.jsp","/**/**.js","/**/**.css").permitAll()
			//	.antMatchers(customProperties.getSecurity().getIgnore().getIgnore()).permitAll()
     	    	//.antMatchers("/**").access("@ignoreCustomAuthority.hasPermission(request, authentication)")//使用自定义配置，对符合要求的目录进行忽视
		;

	}

	@Override
	public int getOrder() {
		return 500;
	}

}
