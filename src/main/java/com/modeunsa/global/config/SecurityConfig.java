package com.modeunsa.global.config;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {
  private final SecurityProperties securityProperties;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin));

    if (securityProperties.isPermitAll()) {
      http.authorizeHttpRequests(auth ->
          auth.anyRequest().permitAll()
      );
    } else {
      String[] permittedUrls = securityProperties.getPermitUrls().toArray(new String[0]);

      http.authorizeHttpRequests(auth ->
          auth.requestMatchers(permittedUrls).permitAll()
              .anyRequest().authenticated()
      );
    }

    return http.build();
  }
}
