package com.yishuifengxiao.common.security.httpsecurity;

import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;

import com.yishuifengxiao.common.security.authorize.SecurityContextManager;

/**
 * <p>资源授权拦截器</p>

 * 配置哪些资源需要经过权限处理
 * 该配置会被
 * <code>SecurityContextManager</code>收集，然后经过<code>config(HttpSecurity http) </code>注入到security中
 * 
 * @see SecurityContextManager
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class HttpSecurityInterceptor
		extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

}
