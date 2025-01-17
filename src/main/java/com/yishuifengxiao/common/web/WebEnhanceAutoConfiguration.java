/**
 *
 */
package com.yishuifengxiao.common.web;

import ch.qos.logback.classic.Level;
import com.yishuifengxiao.common.support.TraceContext;
import com.yishuifengxiao.common.tool.entity.Response;
import com.yishuifengxiao.common.tool.log.LogLevelUtil;
import com.yishuifengxiao.common.tool.random.IdWorker;
import com.yishuifengxiao.common.tool.utils.OsUtils;
import com.yishuifengxiao.common.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * web增强支持配置
 *
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({WebEnhanceProperties.class})
@ConditionalOnProperty(prefix = "yishuifengxiao.web", name = {"enable"}, havingValue = "true", matchIfMissing = true)
public class WebEnhanceAutoConfiguration {

    @Autowired
    private WebEnhanceProperties webEnhanceProperties;

    /**
     * 注入一个跨域支持过滤器
     *
     * @return 跨域支持过滤器
     */
    @Bean("corsAllowedFilter")
    @ConditionalOnMissingBean(name = "corsAllowedFilter")
    @ConditionalOnProperty(prefix = "yishuifengxiao.web.cors", name = {"enable"}, havingValue = "true",
            matchIfMissing = true)
    public FilterRegistrationBean<CustomCorsFilter> corsAllowedFilter() {
        CustomCorsFilter corsFilter = new CustomCorsFilter(webEnhanceProperties.getCors());
        FilterRegistrationBean<CustomCorsFilter> registration = new FilterRegistrationBean<>(corsFilter);
        registration.setName("corsAllowedFilter");
        registration.setUrlPatterns(webEnhanceProperties.getCors().getUrlPatterns());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }


    /**
     * 请求跟踪拦截器用于增加一个请求追踪标志
     *
     * @return
     */
    @Bean("requestTrackingFilter")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @ConditionalOnMissingBean(name = "requestTrackingFilter")
    @ConditionalOnProperty(prefix = "yishuifengxiao.web", name = {"tracked"}, matchIfMissing = true)
    public Filter requestTrackingFilter() {
        return new TracedFilter(webEnhanceProperties);
    }


    /**
     * 全局参数校验功能自动配置
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    @Configuration
    @Aspect
    @ConditionalOnProperty(prefix = "yishuifengxiao.web", name = {"aop"}, havingValue = "true", matchIfMissing = true)
    class ValidAutoConfiguration {

        /**
         * 定义切入点
         */
        @Pointcut("@annotation(org.springframework.web.bind.annotation.ResponseBody) || @annotation(com" +
                ".yishuifengxiao.common.web.annotation.DataValid)")
        public void pointCut() {
        }

        /**
         * 执行环绕通知
         *
         * @param joinPoint ProceedingJoinPoint
         * @return 请求响应结果
         * @throws Throwable 处理时发生异常
         */
        @Around("pointCut()")
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
            // 获取所有的请求参数
            Object[] args = joinPoint.getArgs();
            if (null != args && args.length > 0) {
                for (Object obj : args) {
                    if (obj instanceof BindingResult) {
                        BindingResult errors = (BindingResult) obj;
                        if (errors.hasErrors()) {
                            return Response.badParam(errors.getFieldErrors().get(0).getDefaultMessage());
                        }
                        break;
                    }
                }
            }
            return joinPoint.proceed();

        }

        @PostConstruct
        public void checkConfig() {

            log.trace("【yishuifengxiao-common-spring-boot-starter】: 开启 <全局参数校验功能> 相关的配置");
        }

    }

    @SuppressWarnings("rawtypes")
    @ControllerAdvice
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(DispatcherServlet.class)
    @ConditionalOnProperty(prefix = "yishuifengxiao.web", name = {"tracked"}, matchIfMissing = true)
    class WebResponseBodyAutoConfiguration implements ResponseBodyAdvice {


        @Override
        public boolean supports(MethodParameter returnType, Class converterType) {
            return returnType.hasMethodAnnotation(ResponseBody.class);
        }

        @Override
        public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                      Class selectedConverterType, ServerHttpRequest request,
                                      ServerHttpResponse response) {
            try {
                if (null != body && body instanceof Response) {
                    Response result = (Response) body;
                    Object attribute = null;
                    if (request instanceof ServletServerHttpRequest) {
                        HttpServletRequest httpServerHttpRequest =
                                ((ServletServerHttpRequest) request).getServletRequest();
                        attribute = httpServerHttpRequest.getAttribute(webEnhanceProperties.getTracked());
                    }
                    if (null == attribute || StringUtils.isBlank(attribute.toString())) {
                        attribute = TraceContext.get();
                    }
                    if (null != attribute) {
                        result.setId(attribute.toString());
                    }
                    return result;
                }
            } catch (Exception e) {
                log.debug("【yishuifengxiao-common-spring-boot-starter】:There was a problem obtaining the request " +
                        "tracking id {}", e);
            }

            return body;
        }


        /**
         * 配置检查
         */
        @PostConstruct
        public void checkConfig() {

            log.trace("【yishuifengxiao-common-spring-boot-starter】: 开启 <响应增强功能> 相关的配置");
        }
    }


    /**
     * 配置检查
     */
    @PostConstruct
    public void checkConfig() {
        log.trace("【yishuifengxiao-common-spring-boot-starter】: 开启 <web增强支持> 相关的配置");

    }


    /**
     * 追踪过滤器
     */
    static class TracedFilter extends OncePerRequestFilter {

        private WebEnhanceProperties webEnhanceProperties;

        public TracedFilter(WebEnhanceProperties webEnhanceProperties) {
            this.webEnhanceProperties = webEnhanceProperties;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            try {
                try {
                    String ssid = IdWorker.uuid();
                    request.setAttribute(webEnhanceProperties.getTracked(), ssid);
                    TraceContext.set(ssid);
                    // 动态设置日志
                    String dynamicLogLevel = webEnhanceProperties.getDynamicLogLevel();
                    if (StringUtils.isNotBlank(dynamicLogLevel) && !StringUtils.equalsIgnoreCase("false",
                            dynamicLogLevel)) {
                        // 开启动态日志功能
                        String[] tokens = dynamicLogLevel(request.getHeader(webEnhanceProperties.getDynamicLogLevel()));
                        if (null == tokens) {
                            dynamicLogLevel(request.getParameter(webEnhanceProperties.getDynamicLogLevel()));
                        }
                        if (null != tokens) {
                            LogLevelUtil.setLevel(tokens[0], tokens[1]);
                        }
                    }
                } catch (Exception e) {
                    log.debug("【yishuifengxiao-common-spring-boot-starter】:There was a problem when setting the " +
                            "tracking log and dynamic modification log level. The problem is {}", e);
                }

                filterChain.doFilter(request, response);
            } finally {
                TraceContext.clear();
            }


        }

        /**
         * 解析动态日志功能参数
         *
         * @param text 待解析的文本
         * @return 解析出来的数据
         */
        private String[] dynamicLogLevel(String text) {
            String[] tokens = StringUtils.splitByWholeSeparator(text, OsUtils.COLON);
            if (null == tokens || tokens.length != 2) {
                return null;
            }
            if (StringUtils.isBlank(tokens[0]) || StringUtils.isBlank(tokens[1])) {
                return null;
            }
            Level level = Level.toLevel(tokens[1].trim(), null);
            if (null == level) {
                return null;
            }
            return tokens;
        }

    }

    /**
     * 自定义跨域支持
     *
     * @author yishui
     * @version 1.0.0
     * @since 1.0.0
     */
    static class CustomCorsFilter extends OncePerRequestFilter {

        private WebEnhanceProperties.CorsProperties corsProperties;


        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            try {
                corsProperties.getHeaders().forEach((k, v) -> {
                    if (StringUtils.isNoneBlank(k, v)) {
                        response.setHeader(k.trim(), v.trim());
                    }
                });
                //Vary是一个HTTP响应头，用于指定缓存服务器如何处理不同的客户端请求。当客户端发送一个包含某些请求头信息的请求时，
                // 缓存服务器会检查该请求头信息是否与缓存中的响应匹配。如果匹配成功，则缓存服务器可以直接返回缓存中的响应，而无需向原始服务器发送请求。
                //例如，如果缓存服务器收到一个带有Accept-Encoding:gzip请求头信息的请求，它将检查缓存中是否存在与该请求头信息匹配的响应。
                // 如果存在，则缓存服务器可以直接返回缓存中的压缩响应，而无需向原始服务器发送请求。
                Collection<String> varyHeaders = response.getHeaders(HttpHeaders.VARY);
                if (!varyHeaders.contains(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)) {
                    response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS);
                }
                if (!varyHeaders.contains(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)) {
                    response.addHeader(HttpHeaders.VARY, HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD);
                }


                //Access-Control-Allow-Origin
                String accessControlAllowOrigin = HttpUtils.accessControlAllowOrigin(request);
                //controlAllowHeaders
                accessControlAllowOrigin = Arrays.asList(accessControlAllowOrigin,
                        corsProperties.getAllowedOrigins()).stream().filter(StringUtils::isNotBlank).collect(Collectors.joining(","));


                response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        StringUtils.isBlank(accessControlAllowOrigin) ? "*" : accessControlAllowOrigin);

                String accessControlAllowHeaders = StringUtils.isBlank(corsProperties.getAllowedHeaders()) ?
                        HttpUtils.accessControlAllowHeaders(request, response) : corsProperties.getAllowedHeaders();

                //  Access-Control-Allow-Headers是一个HTTP响应头，用于指定客户端可以在预检请求中使用哪些HTTP请求头信息。预检请求是浏览器在发送跨域请求之前发送的一种OPTIONS
                //  请求，用于检测实际请求是否安全。在预检请求中，浏览器会向服务端发送一些额外的请求头信息，例如Authorization、Content-Type等，以检查服务端是否允许这些请求头信息。
                // 如果服务端不允许某些请求头信息，浏览器将会收到一个错误响应。为了避免这种情况，您可以在服务端的HTTP响应头中添加Access-Control-Allow-Headers
                // 头信息，以允许客户端使用指定的HTTP请求头信息。例如
                response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, accessControlAllowHeaders);
                //  Access-Control-Expose-Headers是一个HTTP响应头，用于指定哪些HTTP响应头信息可以被客户端访问。
                //  默认情况下，客户端只能访问以下响应头信息 ：
                //  Cache-Control 、Content-Language 、Content-Type、Expires、Last-Modified、Pragma
                //  如果您的服务端在响应头中添加了自定义的HTTP响应头信息，例如Authorization，客户端将无法访问该响应头信息
                //  此时，您可以在服务端的HTTP响应头中添加Access-Control-Expose-Headers头信息，以允许客户端访问指定的HTTP响应头信息。
                response.setHeader(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, accessControlAllowHeaders);


                response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        corsProperties.getAllowCredentials() + "");

                response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, corsProperties.getAllowedMethods());


            } catch (Throwable e) {
                if (log.isInfoEnabled()) {
                    log.info("[unkown] 跨域支持捕获到未知异常 {}", e.getMessage());
                }

            }

            filterChain.doFilter(request, response);
        }

        public CustomCorsFilter(WebEnhanceProperties.CorsProperties corsProperties) {
            this.corsProperties = corsProperties;
        }

    }


}
