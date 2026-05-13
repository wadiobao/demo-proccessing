package com.example.demo.config.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.username}")
    private String redisUsername;

    @Value("${spring.data.redis.password}")
    private String redisPassword;
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
    	 RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
         config.setHostName(redisHost);
         config.setPort(redisPort);
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }
    
 // Script 1: Chiếm khóa và kiểm tra data đồng thời
    @Bean
    public DefaultRedisScript<String> lockAndCheckScript() {
        String script = 
            "if redis.call('exists', KEYS[1]) == 1 then return 'HAS_DATA' end " +
            "if redis.call('set', KEYS[2], ARGV[1], 'NX', 'EX', ARGV[2]) then return 'LOCKED' end " +
            "return 'WAIT'";
        return new DefaultRedisScript<>(script, String.class);
    }

    // Script 2: Xóa khóa an toàn (Chỉ chủ sở hữu mới được xóa)
    @Bean
    public DefaultRedisScript<Long> safeUnlockScript() {
        String script = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) " +
            "else return 0 end";
        return new DefaultRedisScript<>(script, Long.class);
    }
}

