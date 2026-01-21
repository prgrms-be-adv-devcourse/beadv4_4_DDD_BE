package com.modeunsa.global.config;

import com.modeunsa.global.filter.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class WebMvcConfig implements WebMvcConfigurer {

  private final RequestLoggingInterceptor requestLoggingInterceptor;
  private final CorsProperties corsProperties;

  @Override
  public void addCorsMappings(CorsRegistry registry) {

    if (!corsProperties.isCorsEnabled()) {
      return;
    }

    registry
        .addMapping("/api/**")
        .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(requestLoggingInterceptor)
        .addPathPatterns("/**")
        .excludePathPatterns(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico",
            "/h2-console/**",
            "/webjars/**",
            "/static/**",
            "/public/**")
        .order(2);
  }
}
