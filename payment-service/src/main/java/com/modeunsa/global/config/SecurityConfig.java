package com.modeunsa.global.config;

import com.modeunsa.global.security.CustomAccessDeniedHandler;
import com.modeunsa.global.security.CustomAuthenticationEntryPoint;
import com.modeunsa.global.security.GatewayHeaderFilter;
import com.modeunsa.global.security.InternalApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** 인증 실패 시 리다이렉트(302 /login) 대신 401 JSON을 반환하여, SPA에서 CORS 없이 로그인 페이지로 처리할 수 있게 함. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

  private final SecurityProperties securityProperties;
  private final GatewayHeaderFilter gatewayHeaderFilter;
  private final InternalApiKeyFilter internalApiKeyFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler));

    if (securityProperties.isPermitAll()) {
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    } else {
      String[] permitUrls = securityProperties.getPermitUrls().toArray(new String[0]);
      http.authorizeHttpRequests(
          auth ->
              auth.requestMatchers("/api/*/*/internal/**")
                  .permitAll()
                  .requestMatchers(permitUrls)
                  .permitAll()
                  .requestMatchers(HttpMethod.POST, "/api/v1/payments")
                  .hasAnyRole("MEMBER", "SELLER", "ADMIN")
                  .requestMatchers(
                      HttpMethod.POST, "/api/v1/payments/*/payment/confirm/by/tossPayments")
                  .hasAnyRole("MEMBER", "SELLER", "ADMIN")
                  .requestMatchers("/api/v1/payments/accounts/**")
                  .hasAnyRole("MEMBER", "SELLER", "ADMIN")
                  .requestMatchers("/api/v1/payments/members/**")
                  .hasAnyRole("MEMBER", "SELLER", "ADMIN")
                  .requestMatchers(HttpMethod.GET, "/api/v2/payments")
                  .hasAnyRole("MEMBER", "SELLER", "ADMIN")
                  .anyRequest()
                  .authenticated());
    }

    http.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(internalApiKeyFilter, GatewayHeaderFilter.class);

    return http.build();
  }
}
