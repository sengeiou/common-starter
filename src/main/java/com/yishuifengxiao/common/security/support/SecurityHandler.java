/**
 *
 */
package com.yishuifengxiao.common.security.support;

import com.yishuifengxiao.common.security.token.SecurityToken;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * 协助处理器
 * </p>
 * <p>
 * 用于在各种 Handler 中根据情况相应地跳转到指定的页面或者输出json格式的数据
 *
 * @author yishui
 * @version 1.0.0
 * @see AuthenticationEntryPoint
 * @see AccessDeniedHandler
 * @see AuthenticationFailureHandler
 * @see AuthenticationFailureHandler
 * @see AuthenticationSuccessHandler
 * @since 1.0.0
 */
public interface SecurityHandler {

    /**
     * 登陆成功后的处理
     *
     * @param propertyResource 系统里配置的资源
     * @param request          HttpServletRequest
     * @param response         HttpServletResponse
     * @param authentication   认证信息
     * @param token            生成的token
     * @throws IOException 处理时发生问题
     */
    void whenAuthenticationSuccess(PropertyResource propertyResource, HttpServletRequest request,
                                   HttpServletResponse response, Authentication authentication, SecurityToken token) throws IOException;

    /**
     * 登陆失败后的处理
     *
     * @param propertyResource 系统里配置的资源
     * @param request          HttpServletRequest
     * @param response         HttpServletResponse
     * @param exception        失败的原因
     * @throws IOException 处理时发生问题
     */
    void whenAuthenticationFailure(PropertyResource propertyResource, HttpServletRequest request,
                                   HttpServletResponse response, Exception exception) throws IOException;

    /**
     * 退出成功后的处理
     *
     * @param propertyResource 系统里配置的资源
     * @param request          HttpServletRequest
     * @param response         HttpServletResponse
     * @param authentication   认证信息
     * @throws IOException 处理时发生问题
     */
    void whenLogoutSuccess(PropertyResource propertyResource, HttpServletRequest request,
                           HttpServletResponse response, Authentication authentication) throws IOException;

    /**
     * <p>
     * 访问资源时权限被拒绝
     * </p>
     * 本身是一个合法的用户，但是对于部分资源没有访问权限
     *
     * @param propertyResource 系统里配置的资源
     * @param request          HttpServletRequest
     * @param response         HttpServletResponse
     * @param exception        被拒绝的原因
     * @throws IOException 处理时发生问题
     */
    void whenAccessDenied(PropertyResource propertyResource, HttpServletRequest request, HttpServletResponse response
            , AccessDeniedException exception) throws IOException;

    /**
     * <p>
     * 访问资源时因为权限等原因发生了异常后的处理
     * </p>
     * 可能本身就不是一个合法的用户
     *
     * @param propertyResource 系统里配置的资源
     * @param request          HttpServletRequest
     * @param response         HttpServletResponse
     * @param exception        发生异常的原因
     * @throws IOException 处理时发生问题
     */
    void onException(PropertyResource propertyResource, HttpServletRequest request, HttpServletResponse response,
                     Exception exception) throws IOException;


}
