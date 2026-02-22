package com.modeunsa.global.config;

import com.modeunsa.global.security.InternalApiKeyFilter;
import com.modeunsa.global.security.jwt.JwtAccessDeniedHandler;
import com.modeunsa.global.security.jwt.JwtAuthenticationEntryPoint;
import com.modeunsa.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
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
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
  private final InternalApiKeyFilter internalApiKeyFilter;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
        .exceptionHandling(
            exception ->
                exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(jwtAccessDeniedHandler));

    if (securityProperties.isPermitAll()) {
      http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    } else {
      String[] permitUrls = securityProperties.getPermitUrls().toArray(new String[0]);

      http.authorizeHttpRequests(
          auth ->
              auth.requestMatchers("/api/*/*/internal/**")
                  .permitAll()

                  // ========================================
                  // 공개 URL (yml에서 관리)
                  // ========================================
                  .requestMatchers(permitUrls)
                  .permitAll()

                  // ========================================
                  // 가입 대기(PRE_ACTIVE) 회원 관련 API
                  // ========================================
                  // 회원 가입 완료 페이지
                  .requestMatchers(HttpMethod.POST, "/api/v2/members/signup-complete")
                  .hasRole("PRE_ACTIVE")

                  // 기본 정보 조회
                  .requestMatchers("/api/v1/members/me/basic-info")
                  .hasAnyRole("PRE_ACTIVE", "MEMBER")

                  // ========================================
                  // 관리자 전용
                  // ========================================
                  .requestMatchers("/api/v1/admin/**")
                  .hasRole("ADMIN")

                  // ========================================
                  // 판매자 전용
                  // ========================================
                  // 판매자 정보
                  .requestMatchers(HttpMethod.GET, "/api/v2/members/seller")
                  .hasRole("SELLER")

                  // ========================================
                  // 회원 전용
                  // ========================================
                  // 마이페이지
                  .requestMatchers("/api/v1/members/me/**")
                  .hasRole("MEMBER")

                  // ========================================
                  // 나머지는 인증 필요
                  // ========================================
                  .anyRequest()
                  .authenticated());
    }
    http.addFilterBefore(internalApiKeyFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtAuthenticationFilter, InternalApiKeyFilter.class);
    return http.build();
  }

  /** Role Hierarchy 설정 */
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SYSTEM")
        .implies("ADMIN") // SYSTEM은 ADMIN의 모든 권한을 가짐
        .role("HOLDER")
        .implies("ADMIN") // HOLDER는 ADMIN의 모든 권한을 가짐
        .role("ADMIN")
        .implies("SELLER") // ADMIN은 SELLER의 모든 권한을 가짐
        .role("SELLER")
        .implies("MEMBER") // SELLER는 MEMBER의 모든 권한을 가짐
        .build();
  }
}
