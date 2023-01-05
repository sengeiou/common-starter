package com.yishuifengxiao.common.security.utils;

import javax.servlet.http.HttpServletRequest;

import com.yishuifengxiao.common.security.extractor.SecurityExtractor;
import com.yishuifengxiao.common.security.support.SecurityHelper;
import com.yishuifengxiao.common.security.token.SecurityToken;
import com.yishuifengxiao.common.tool.exception.CustomException;

/**
 * 令牌生成工具
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public class TokenUtil {

	private static SecurityHelper securityHelper;

	private static SecurityExtractor securityExtractor;

	/**
	 * 生成一个令牌
	 * 
	 * @param username 用户账号
	 * @param password 账号对应的密码
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken create(String username, String password) throws CustomException {

		return securityHelper.create(username, password, null);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param request  HttpServletRequest
	 * @param username 用户账号
	 * @param password 账号对应的密码
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken create(HttpServletRequest request, String username, String password)
			throws CustomException {
		String sessionId = securityExtractor.extractUserUniqueIdentitier(request, null);
		return create(username, password, sessionId);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param username  用户账号
	 * @param password  账号对应的密码
	 * @param sessionId 会话id
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken create(String username, String password, String sessionId) throws CustomException {

		return securityHelper.create(username, password, sessionId);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param username 用户账号
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(String username) throws CustomException {

		return securityHelper.createUnsafe(username, null);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param request  HttpServletRequest
	 * @param username 用户账号
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(HttpServletRequest request, String username) throws CustomException {
		String sessionId = securityExtractor.extractUserUniqueIdentitier(request, null);
		return securityHelper.createUnsafe(username, sessionId);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param username     用户账号
	 * @param validSeconds 令牌过期时间，单位为秒
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(String username, int validSeconds) throws CustomException {
		return createUnsafe(username, null, validSeconds);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param request      HttpServletRequest
	 * @param username     用户账号
	 * @param validSeconds 令牌过期时间，单位为秒
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(HttpServletRequest request, String username, int validSeconds)
			throws CustomException {
		String sessionId = securityExtractor.extractUserUniqueIdentitier(request, null);
		return createUnsafe(username, sessionId, validSeconds);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param username  用户账号
	 * @param sessionId 会话id
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(String username, String sessionId) throws CustomException {

		return securityHelper.createUnsafe(username, sessionId);
	}

	/**
	 * 生成一个令牌
	 * 
	 * @param username     用户账号
	 * @param sessionId    会话id
	 * @param validSeconds 令牌过期时间，单位为秒
	 * @return 生成的令牌
	 * @throws CustomException 非法的用户信息或状态
	 */
	public static SecurityToken createUnsafe(String username, String sessionId, int validSeconds)
			throws CustomException {

		return securityHelper.createUnsafe(username, sessionId, validSeconds);
	}

	public TokenUtil(SecurityHelper securityHelper, SecurityExtractor securityExtractor) {
		TokenUtil.securityHelper = securityHelper;
		TokenUtil.securityExtractor = securityExtractor;
	}

}