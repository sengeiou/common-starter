package com.yishuifengxiao.common.security.user.encoder;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 自定义加密类
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 *
 */
public interface BasePasswordEncoder extends PasswordEncoder {

	/**
	 * 对加密后的密码进行解密
	 * 
	 * @param encodedPassword 需要解密的数据【不能为空】
	 * @return 解密后的数据，null表示解密失败
	 */
	String decode(String encodedPassword);

	/**
	 * 是否支持解密
	 * 
	 * @return true表示支持，false表示不支持
	 */
	default boolean supportDecode() {
		return false;
	};

}