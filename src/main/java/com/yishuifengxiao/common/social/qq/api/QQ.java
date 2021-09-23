package com.yishuifengxiao.common.social.qq.api;

import com.yishuifengxiao.common.social.qq.entity.QqUserInfo;

/**
 * <p>【传入参数为 】：accessToken 和 accessToken
 * 【输出的响应为】： 用户信息
 * 根据【accessToken】 和 【appId】 获取用户信息</p>
 * 
 * <pre>
 * 1. 使用https://graph.qq.com/oauth2.0/me 接口通过accessToken 获取到client_id 和 openid
 * () 
 * 
 * 文档参见
 * https://wiki.connect.qq.com/%E8%8E%B7%E5%8F%96%E7%94%A8%E6%88%B7openid_oauth2-0
 *
 * 
 * 2. 使用https://graph.qq.com/user/get_user_info接口通过 appId 和 openId 获取到用户信息 ( 注意:
 * oauth_consumer_key就是 appId )
 * 
 * </pre>
 * 文档参见 https://wiki.connect.qq.com/get_user_info
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
public interface QQ {
	/**
	 * 获取用户信息
	 * 
	 * @return 用户信息
	 */
	QqUserInfo getUserInfo();
}
