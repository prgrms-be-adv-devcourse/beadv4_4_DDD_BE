package com.modeunsa.global.config;

import com.modeunsa.global.security.jwt.JwtAccessDeniedHandler;
import com.modeunsa.global.security.jwt.JwtAuthenticationEntryPoint;
import com.modeunsa.global.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

  private final SecurityProperties securityProperties;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
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
              auth
                  // ========================================
                  // 1. 공개 URL (yml에서 관리)
                  // ========================================
                  .requestMatchers(permitUrls)
                  .permitAll()

                  // ========================================
                  // 2. 상품 API - GET만 공개
                  // ========================================
                  .requestMatchers(HttpMethod.GET, "/api/v1/products")
                  .permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/v1/products/{id}")
                  .permitAll()

                  // ========================================
                  // 3. 관리자 전용
                  // ========================================
                  .requestMatchers("/api/v1/admin/**")
                  .hasRole("ADMIN")

                  // ========================================
                  // 4. 판매자 전용
                  // ========================================
                  // 상품 CUD
                  .requestMatchers(HttpMethod.POST, "/api/v1/products")
                  .hasRole("SELLER")
                  .requestMatchers(HttpMethod.PATCH, "/api/v1/products/**")
                  .hasRole("SELLER")

                  // 정산
                  .requestMatchers("/api/v1/settlements/**")
                  .hasRole("SELLER")

                  // ========================================
                  // 5. 회원 전용
                  // ========================================
                  // 마이페이지
                  .requestMatchers("/api/members/me/**")
                  .hasRole("MEMBER")

                  // 판매자 등록 신청
                  .requestMatchers(HttpMethod.POST, "/api/v1/sellers/register")
                  .hasRole("MEMBER")

                  // 관심상품
                  .requestMatchers(HttpMethod.POST, "/api/v1/products/*/favorite")
                  .hasRole("MEMBER")
                  .requestMatchers(HttpMethod.DELETE, "/api/v1/products/*/favorite")
                  .hasRole("MEMBER")

                  // 주문
                  .requestMatchers("/api/v1/orders/**")
                  .hasRole("MEMBER")

                  // 결제
                  .requestMatchers(HttpMethod.POST, "/api/v1/payments")
                  .hasRole("MEMBER")
                  .requestMatchers("/api/v1/payments/accounts/**")
                  .hasRole("MEMBER")
                  .requestMatchers("/api/v1/payments/members/**")
                  .hasRole("MEMBER")

                  // ========================================
                  // 6. 나머지는 인증 필요
                  // ========================================
                  .anyRequest()
                  .authenticated());
    }

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  /**
   * Role 계층 구조 업데이트: - SYSTEM > ADMIN - HOLDER > ADMIN - ADMIN > SELLER > MEMBER 결과적으로 SYSTEM과
   * HOLDER는 모든 권한(ADMIN, SELLER, MEMBER)을 포함합니다.
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SYSTEM")
        .implies("ADMIN") // SYSTEM은 ADMIN의 모든 권한을 가짐
        .role("HOLDER")
        .implies("ADMIN") // HOLDER는 ADMIN의 모든 권한을 가짐
        .role("ADMIN")
        .implies("SELLER") // 기존 계층 유지
        .role("SELLER")
        .implies("MEMBER")
        .build();
  }

  /** Method Security에도 Role 계층 적용 */
  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // 1. 허용할 출처 (프론트엔드 주소)
    configuration.addAllowedOrigin("https://modeunsa.store");
    configuration.addAllowedOrigin("https://www.modeunsa.store");
    configuration.addAllowedOrigin("http://localhost:3000"); // 로컬 테스트용

    // 2. 허용할 HTTP 메서드
    configuration.addAllowedMethod("*"); // GET, POST, PUT, DELETE 등 모두 허용

    // 3. 허용할 헤더
    configuration.addAllowedHeader("*");

    // 4. 자격 증명 허용 (쿠키나 JWT를 헤더에 담아 보낼 때 필수)
    configuration.setAllowCredentials(true);

    // 5. 브라우저가 응답에서 접근할 수 있는 헤더 (JWT 사용 시 필요할 수 있음)
    configuration.addExposedHeader("Authorization");

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration); // 모든 경로에 적용
    return source;
  }
}
