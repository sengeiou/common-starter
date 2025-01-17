package com.yishuifengxiao.common.security.httpsecurity.authorize.impl;

import com.yishuifengxiao.common.security.httpsecurity.AuthorizeProvider;
import com.yishuifengxiao.common.security.httpsecurity.authorize.custom.CustomResourceProvider;
import com.yishuifengxiao.common.security.support.AuthenticationPoint;
import com.yishuifengxiao.common.security.support.PropertyResource;
import com.yishuifengxiao.common.security.utils.ExcludeRequestMatcher;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 资源设置处理器
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class ResourceAuthorizeProvider implements AuthorizeProvider {


    /**
     * key CustomResourceProvider的名字
     * value CustomResourceProvider的实例
     */
    private Map<String, CustomResourceProvider> customResourceProviders;

    @Override
    public void apply(PropertyResource propertyResource, AuthenticationPoint authenticationPoint, HttpSecurity http) throws Exception {


        final ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry =
                http.authorizeRequests();

        registry.mvcMatchers(HttpMethod.OPTIONS).permitAll();
        registry.antMatchers(HttpMethod.OPTIONS).permitAll();
//        registry.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll();
        // 所有忽视的资源
        for (String url : propertyResource.allIgnoreUrls()) {
            registry.antMatchers(url).permitAll();
        }
        // 所有直接放行的资源
        for (String url : propertyResource.allPermitUrs()) {
            registry.antMatchers(url).permitAll();
        }

        for (String url : propertyResource.anonymousUrls()) {
            registry.antMatchers(url).anonymous();
        }
        // 所有已经明确了权限的路径
        Set<String> urls = new HashSet<>();
        urls.addAll(Arrays.stream(propertyResource.allIgnoreUrls()).collect(Collectors.toSet()));
        urls.addAll(propertyResource.allPermitUrs());
        urls.addAll(propertyResource.anonymousUrls());
        List<String> list = urls.stream().filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        final ExcludeRequestMatcher matcher = new ExcludeRequestMatcher(list);
        // 所有自定义权限路径的资源
        if (null != this.customResourceProviders) {
            customResourceProviders.forEach((providerName, provider) -> {
                registry.requestMatchers(provider.requestMatcher()).access("@" + providerName + ".hasPermission" +
                        "(request, authentication)");
                // 增加配置
                matcher.addRequestMatcher(provider.requestMatcher());
            });
        }
        //只要经过了授权就能访问
        registry.requestMatchers(matcher).authenticated();

    }

    @Override
    public int order() {
        return Integer.MAX_VALUE;
    }

    public Map<String, CustomResourceProvider> getCustomResourceProviders() {
        return customResourceProviders;
    }

    public void setCustomResourceProviders(Map<String, CustomResourceProvider> customResourceProviders) {
        this.customResourceProviders = customResourceProviders;
    }
}
