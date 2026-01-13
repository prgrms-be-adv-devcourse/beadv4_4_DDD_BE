package com.modeunsa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers("/h2-console/**").permitAll().anyRequest().authenticated())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));
    return http.build();
  }
}
