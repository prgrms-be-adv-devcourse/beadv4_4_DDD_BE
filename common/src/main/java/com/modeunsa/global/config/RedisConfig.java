package com.modeunsa.global.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@RequiredArgsConstructor
public class RedisConfig {

  private final RedisProperties redisProperties;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // 1. Redis 연결 정보 설정 (호스트, 포트, 비밀번호)
    RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
    redisConfig.setHostName(redisProperties.getHost());
    redisConfig.setPort(redisProperties.getPort());

    // 비밀번호가 설정되어 있을 경우에만 적용
    String password = redisProperties.getPassword();
    if (StringUtils.hasText(password)) {
      redisConfig.setPassword(password);
    }

    // 2. Lettuce 클라이언트 옵션 설정 (프로토콜 버전을 RESP2로 강제)
    // 이 부분이 없으면 Redis 버전에 따라 "NOAUTH HELLO" 에러가 날 수 있습니다.
    LettuceClientConfiguration clientConfig =
        LettuceClientConfiguration.builder()
            .clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build())
            .build();

    // 3. 설정이 적용된 팩토리 반환
    return new LettuceConnectionFactory(redisConfig, clientConfig);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // String 직렬화 설정
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new StringRedisSerializer());

    return template;
  }
}
