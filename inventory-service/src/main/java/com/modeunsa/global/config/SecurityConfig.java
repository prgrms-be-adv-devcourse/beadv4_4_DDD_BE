package com.modeunsa.global.config;

import com.modeunsa.global.security.CustomAccessDeniedHandler;
import com.modeunsa.global.security.CustomAuthenticationEntryPoint;
import com.modeunsa.global.security.GatewayHeaderFilter;
import com.modeunsa.global.security.InternalApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final SecurityProperties securityProperties;
  private final GatewayHeaderFilter gatewayHeaderFilter;
  private final InternalApiKeyFilter internalApiKeyFilter;
  private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
  private final CustomAccessDeniedHandler customAccessDeniedHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
        .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(internalApiKeyFilter, GatewayHeaderFilter.class);

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
                  .anyRequest()
                  .authenticated());
    }

    return http.build();
  }
}
