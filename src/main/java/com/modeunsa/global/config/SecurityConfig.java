package com.modeunsa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Profile("!dev")
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String[] PERMIT_URLS = {
      "/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/api/v1/auths/oauth/**"
  };

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    /**
     *  이 서버는 Stateless API 서버이며, 인증은 Authorization Header(JWT)를 통해 수행됨.
     *  쿠키/세션 기반 인증을 사용하지 않으므로 Spring Security CSRF 보호 대상이 아님.
     *  OAuth 로그인 과정의 CSRF는 state 파라미터로 별도 방어함.
     */
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(PERMIT_URLS).permitAll().anyRequest().authenticated())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));
    return http.build();
  }
}