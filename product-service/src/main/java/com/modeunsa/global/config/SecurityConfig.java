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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({SecurityProperties.class})
@RequiredArgsConstructor
public class SecurityConfig {

  private final SecurityProperties securityProperties;
  private final GatewayHeaderFilter gatewayHeaderFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;
  private final InternalApiKeyFilter internalApiKeyFilter;

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

                  // 공개 URL (yml에서 관리)
                  .requestMatchers(permitUrls)
                  .permitAll()

                  //                // 상품 API - GET만 공개
                  .requestMatchers(HttpMethod.GET, "/api/v1/products")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/v1/products/{id:[0-9]+}")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/v2/products/search/**")
                  .permitAll()

                  // ========================================
                  // 어드민 전용
                  // ========================================
                  // TODO: admin 전용 처리 후 주석 제거
                  //                  .requestMatchers(HttpMethod.POST,
                  // "/api/v1/products/search/reindex")
                  //                  .hasRole("ADMIN")

                  // ========================================
                  // 판매자 전용
                  // ========================================
                  // 상품 CRUD
                  .requestMatchers(HttpMethod.POST, "/api/v1/products")
                  .hasRole("SELLER")
                  .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**")
                  .hasRole("SELLER")
                  .requestMatchers(HttpMethod.GET, "/api/v1/products/sellers/**")
                  .hasRole("SELLER")

                  // ========================================
                  // 회원 전용
                  // ========================================
                  // 관심상품
                  .requestMatchers(HttpMethod.GET, "/api/v1/products/favorites")
                  .hasRole("MEMBER")
                  .requestMatchers(HttpMethod.POST, "/api/v1/products/favorite/**")
                  .hasRole("MEMBER")
                  .requestMatchers(HttpMethod.DELETE, "/api/v1/products/favorite/**")
                  .hasRole("MEMBER")

                  // 나머지는 인증 필요
                  .anyRequest()
                  .authenticated());
    }

    // 3. 필터 등록 순서: Internal 검사 -> Gateway 헤더 검사 -> UsernamePassword 검사
    http.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(internalApiKeyFilter, GatewayHeaderFilter.class);

    return http.build();
  }
}
