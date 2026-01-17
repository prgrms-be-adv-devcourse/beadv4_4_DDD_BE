package com.modeunsa.global.config;

import com.modeunsa.global.filter.RequestLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final RequestLoggingInterceptor requestLoggingInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(requestLoggingInterceptor).addPathPatterns("/**").order(2);
  }
}
