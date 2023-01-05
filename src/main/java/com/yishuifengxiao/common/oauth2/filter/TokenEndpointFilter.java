/**
 * 
 */
package com.yishuifengxiao.common.oauth2.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.yishuifengxiao.common.oauth2.Oauth2Properties;
import com.yishuifengxiao.common.security.processor.HandlerProcessor;
import com.yishuifengxiao.common.security.resource.PropertyResource;
import com.yishuifengxiao.common.security.support.SecurityHelper;
import com.yishuifengxiao.common.tool.entity.Response;
import com.yishuifengxiao.common.tool.exception.CustomException;
import com.yishuifengxiao.common.utils.HttpExtractor;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用于oauth2密码模式下载提前校验用户名和密码是否正确
 * </p>
 * 在 <code>Oauth2Server</code>中被使用
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class TokenEndpointFilter extends OncePerRequestFilter {

	private static final AntPathRequestMatcher MATCHER = new AntPathRequestMatcher("/oauth/token");

	private HttpExtractor httpExtractor = new HttpExtractor();

	private final static String BASIC = "basic ";

	private static final String USERNAME = "username";

	private static final String PASSWORD = "password";

	private static final String GRANT_TYPE = "grant_type";

	private static final String PARAM_VALUE = "password";

	/**
	 * 是否显示日志
	 */
	private boolean show = false;

	/**
	 * 协助处理器
	 */
	private HandlerProcessor handlerProcessor;

	private PropertyResource propertyResource;

	private SecurityHelper securityHelper;

	private ClientDetailsService clientDetailsService;

	private PasswordEncoder passwordEncoder;

	private Oauth2Properties oauth2Properties;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		if (MATCHER.matches(httpServletRequest)) {

			String header = request.getHeader("Authorization");

			if (header == null || !header.toLowerCase().startsWith(BASIC)) {
				chain.doFilter(request, response);
				return;
			}

			try {
				String[] tokens = httpExtractor.extractBaiscAuth(request);

				String clientId = tokens[0];

				if (this.show) {
					log.info("Basic Authentication Authorization header found for user '" + clientId + "'");
				}

				ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);

				if (clientDetails == null) {
					// 终端不存在
					handlerProcessor.preAuth(request, response, Response.of(oauth2Properties.getInvalidClientCode(),
							oauth2Properties.getClientNotExtis(), null));
					return;
				}
				if (!passwordEncoder.matches(tokens[1], clientDetails.getClientSecret())) {

					// 密码错误
					handlerProcessor.preAuth(request, response, Response.of(oauth2Properties.getInvalidClientCode(),
							oauth2Properties.getPwdErrorMsg(), null));
					return;
				}

				// 授权类型
				String grantType = httpServletRequest.getParameter(GRANT_TYPE);
				if (StringUtils.containsIgnoreCase(PARAM_VALUE, grantType)) {
					// 密码模式

					String username = httpServletRequest.getParameter(USERNAME);
					String password = httpServletRequest.getParameter(PASSWORD);

					if (this.show) {
						log.info("The user name obtained in oauth2 password mode is {} ", username);
					}

					try {
						securityHelper.authorize(username, password);
					} catch (CustomException exception) {
						handlerProcessor.preAuth(httpServletRequest, httpServletResponse,
								Response.of(propertyResource.security().getMsg().getInvalidLoginParamCode(),
										exception.getMessage(), exception));
						return;
					}

				}

			} catch (CustomException e) {
				handlerProcessor.preAuth(request, response, Response.of(oauth2Properties.getInvalidClientCode(),
						oauth2Properties.getInvalidBasicToken(), e));
				return;
			} catch (Exception e) {
				// 其他异常
				handlerProcessor.exception(propertyResource, request, response, e);
				return;
			}

		}

		chain.doFilter(request, response);

	}

	public TokenEndpointFilter(HandlerProcessor handlerProcessor, PropertyResource propertyResource,
			SecurityHelper securityHelper, ClientDetailsService clientDetailsService, PasswordEncoder passwordEncoder,
			Oauth2Properties oauth2Properties) {
		this.handlerProcessor = handlerProcessor;
		this.propertyResource = propertyResource;
		this.securityHelper = securityHelper;
		this.clientDetailsService = clientDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.oauth2Properties = oauth2Properties;
	}

}