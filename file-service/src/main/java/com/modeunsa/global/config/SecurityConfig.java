package com.modeunsa.global.config;

import com.modeunsa.global.security.CustomAccessDeniedHandler;
import com.modeunsa.global.security.CustomAuthenticationEntryPoint;
import com.modeunsa.global.security.GatewayHeaderFilter;
import com.modeunsa.global.security.InternalApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

                  // 공개 URL (yml에서 관리 - Swagger, Health 등)
                  .requestMatchers(permitUrls)
                  .permitAll()

                  // File 관련 API 권한 설정
                  .requestMatchers("/api/v1/files/**")
                  .hasAnyRole("PRE_ACTIVE", "MEMBER")

                  // 나머지는 인증 필요
                  .anyRequest()
                  .authenticated());
    }

    // 3. 필터 등록 순서: Internal 검사 -> Gateway 헤더 검사 -> UsernamePassword 검사
    http.addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(internalApiKeyFilter, GatewayHeaderFilter.class);

    return http.build();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SYSTEM")
        .implies("ADMIN")
        .role("HOLDER")
        .implies("ADMIN")
        .role("ADMIN")
        .implies("SELLER")
        .role("SELLER")
        .implies("MEMBER")
        .build();
  }
}
