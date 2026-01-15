package com.modeunsa.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${security.permit-all:false}")
  private boolean permitAll;

  @Value("${security.permit-urls}")
  private String[] permitUrls;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));

    if (permitAll) {
      http.authorizeHttpRequests(auth ->
          auth.anyRequest().permitAll()
      );
    } else {
      http.authorizeHttpRequests(auth ->
          auth.requestMatchers(permitUrls).permitAll()
              .anyRequest().authenticated()
      );
    }

    return http.build();
  }
}