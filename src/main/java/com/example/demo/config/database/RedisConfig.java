package com.example.demo.config.database;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.TimeoutOptions.TimeoutSource;
import io.lettuce.core.protocol.CommandArgsAccessor;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.RedisCommand;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration.LettuceClientConfigurationBuilder;

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
    public RedisConnectionFactory redisConnectionFactory(LettuceClientConfiguration clientConfiguration) {
    	 RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
         config.setHostName(redisHost);
         config.setPort(redisPort);
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
        return new LettuceConnectionFactory(config, clientConfiguration);
    }

    @Bean
    public LettuceClientConfiguration lettuceClientConfiguration(ObjectProvider<LettuceClientConfigurationBuilderCustomizer> customizers) {
        LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
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
    
    @Bean
    public LettuceClientConfigurationBuilderCustomizer customizer() {
        return builder -> {
            // Định nghĩa TimeoutSource như bạn đã viết
            TimeoutOptions timeoutOptions = TimeoutOptions.builder()
                .timeoutSource(new TimeoutSource() {
                    @Override
                    public long getTimeout(RedisCommand<?, ?, ?> command) {
                        if (command.getType() == CommandType.BLPOP || command.getType() == CommandType.BRPOP) {
                            // Lấy tham số timeout từ chính câu lệnh Redis
                            // Lưu ý: Cần cộng thêm một khoảng đệm nhỏ (ví dụ 1-2s) để driver không ngắt trước Redis
                            return TimeUnit.SECONDS.toNanos(CommandArgsAccessor.getFirstInteger(command.getArgs()) + 2);
                        }
                        return -1; // Quay về mặc định
                    }
                }).build();

            builder.clientOptions(ClientOptions.builder()
                .timeoutOptions(timeoutOptions)
                .build());
        };
    }
}

