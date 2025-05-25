package org.example.booknuri.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfig {

    // Redis 연결 팩토리 등록
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }

    // RedisTemplate<String, Object> + 직렬화 설정 추가
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());

        // JSON 직렬화 설정 (Jackson 사용)
        template.setKeySerializer(new StringRedisSerializer()); // 키는 그냥 문자열
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 값은 JSON

        return template;
    }
}
