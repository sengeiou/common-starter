package com.yishuifengxiao.common.redis;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Redis扩展支持属性配置
 * 
 * @author yishui
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "yishuifengxiao.redis")
public class RedisProperties {

	/**
	 * 是否开启Redis配置功能，默认为开启
	 */
	private Boolean enable = true;

	/**
	 * 缓存过期时间。默认为30分钟
	 */
	private Duration ttl = Duration.ofMinutes(30L);

}
