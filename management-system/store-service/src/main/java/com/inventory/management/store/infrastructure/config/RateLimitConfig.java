package com.inventory.management.store.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.List;

/**
 * Configuração de Rate Limiting usando Redis.
 * 
 * @author System
 * @version 1.0.0
 */
@Configuration
@Slf4j
public class RateLimitConfig {
    
    @Bean
    public RedisTemplate<String, String> rateLimitRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
    
    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText("""
            local key = KEYS[1]
            local window = tonumber(ARGV[1])
            local limit = tonumber(ARGV[2])
            local current_time = tonumber(ARGV[3])
            
            -- Clean old entries
            redis.call('ZREMRANGEBYSCORE', key, 0, current_time - window * 1000)
            
            -- Count current requests
            local current = redis.call('ZCARD', key)
            
            if current < limit then
                -- Add current request
                redis.call('ZADD', key, current_time, current_time)
                redis.call('EXPIRE', key, window)
                return limit - current - 1
            else
                return -1
            end
            """);
        script.setResultType(Long.class);
        return script;
    }
    
    @Bean
    public RateLimiter rateLimiter(RedisTemplate<String, String> rateLimitRedisTemplate, 
                                   DefaultRedisScript<Long> rateLimitScript) {
        return new RateLimiter(rateLimitRedisTemplate, rateLimitScript);
    }
    
    public static class RateLimiter {
        private final RedisTemplate<String, String> redisTemplate;
        private final DefaultRedisScript<Long> script;
        
        public RateLimiter(RedisTemplate<String, String> redisTemplate, DefaultRedisScript<Long> script) {
            this.redisTemplate = redisTemplate;
            this.script = script;
        }
        
        public boolean isAllowed(String key, int limit, Duration window) {
            long currentTime = System.currentTimeMillis();
            String redisKey = "rate_limit:" + key;
            
            try {
                Long result = redisTemplate.execute(script, 
                    List.of(redisKey), 
                    String.valueOf(window.getSeconds()), 
                    String.valueOf(limit), 
                    String.valueOf(currentTime));
                
                return result != null && result >= 0;
            } catch (Exception e) {
                log.error("Rate limiting failed for key: {}", key, e);
                // Fail open - allow request if Redis is down
                return true;
            }
        }
        
        public long getRemainingRequests(String key, int limit, Duration window) {
            long currentTime = System.currentTimeMillis();
            String redisKey = "rate_limit:" + key;
            
            try {
                Long result = redisTemplate.execute(script, 
                    List.of(redisKey), 
                    String.valueOf(window.getSeconds()), 
                    String.valueOf(limit), 
                    String.valueOf(currentTime));
                
                return result != null ? Math.max(0, result) : limit;
            } catch (Exception e) {
                log.error("Failed to get remaining requests for key: {}", key, e);
                return limit;
            }
        }
    }
}
