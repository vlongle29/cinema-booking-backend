package com.example.CineBook.common.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableRedisRepositories(basePackages = "com.example.CineBook.repository.redis")
@EnableCaching
public class RedisConfig {
    /**
     * ObjectMapper chính cho ứng dụng (sử dụng cho API).
     * Bean này là `@Primary` để đảm bảo nó là lựa chọn mặc định của Spring cho việc serialize/deserialize JSON.
     * Nó không chứa cấu hình `activateDefaultTyping` để tránh thêm trường `@class` vào API response.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Good practice
        return objectMapper;
    }

    /**
     * Cấu hình ObjectMapper chuẩn cho việc serialize/deserialize với Redis.
     * Bean này sẽ được tái sử dụng bởi cả CacheManager và RedisTemplate.
     *
     * @return ObjectMapper đã được cấu hình cho Redis.
     */
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper redisObjectMapper = new ObjectMapper();
        redisObjectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        redisObjectMapper.registerModule(new JavaTimeModule());

        redisObjectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return redisObjectMapper;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10)) // TTL 10 phút
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(config)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}


/**
 *
 * @EnableRedisRepositories(basePackages = "com.metasol.cms.repository.redis")
 * Mục đích: Kích hoạt Spring Data Redis Repository
 *
 * Chức năng:
 * Tự động tạo implementation cho các Redis repository interfaces
 * Quét package được chỉ định để tìm repository interfaces
 * Cho phép sử dụng các method như save(), findById(), existsByToken()
 *
 * Chú ý: Nếu không có @EnableRedisRepositories thì repository sẽ không được spring nhận diện và tạo Bean
 *
 */
