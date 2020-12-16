package com.yishuifengxiao.common.security.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import com.yishuifengxiao.common.security.AbstractSecurityConfig;
import com.yishuifengxiao.common.security.remerberme.RedisTokenRepository;
import com.yishuifengxiao.common.security.token.holder.TokenHolder;
import com.yishuifengxiao.common.security.token.holder.impl.RedisTokenHolder;

/**
 * 配置基于Redis的记住密码策略配置
 * 
 * @author yishui
 * @date 2019年10月18日
 * @version 1.0.0
 */
@Configuration
@ConditionalOnBean(AbstractSecurityConfig.class)
@ConditionalOnClass({ DefaultAuthenticationEventPublisher.class, EnableWebSecurity.class,
		WebSecurityConfigurerAdapter.class, RedisOperations.class})
@ConditionalOnMissingBean(name = "persistentTokenRepository")
@ConditionalOnProperty(prefix = "yishuifengxiao.security", name = {
		"enable" }, havingValue = "true", matchIfMissing = true)
public class SecurityRedisAutoConfiguration {
	/**
	 * 记住密码策略【存储在redis数据库中】
	 * 
	 * @return
	 */
	@Bean("persistentTokenRepository")
	@ConditionalOnBean(name = "redisTemplate")
	@ConditionalOnClass(DefaultAuthenticationEventPublisher.class)
	public PersistentTokenRepository redisTokenRepository(RedisTemplate<String, Object> redisTemplate) {
		RedisTokenRepository redisTokenRepository = new RedisTokenRepository();
		redisTokenRepository.setRedisTemplate(redisTemplate);
		return new RedisTokenRepository();
	}
	
	@ConditionalOnBean(name = "redisTemplate")
	@Bean("tokenHolder")
	@ConditionalOnMissingBean
	public TokenHolder tokenHolder(RedisTemplate<String, Object> redisTemplate){
		RedisTokenHolder redisTokenHolder=new RedisTokenHolder();
		redisTokenHolder.setRedisTemplate(redisTemplate);
		return redisTokenHolder;
	}
}
