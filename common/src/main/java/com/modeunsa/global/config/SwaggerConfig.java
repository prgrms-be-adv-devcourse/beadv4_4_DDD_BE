package com.modeunsa.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class SwaggerConfig {

  @Value("${custom.swagger.serverUrl:http://localhost}")
  private String serverUrl;

  @Value("${custom.swagger.description}")
  private String description;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().addServersItem(new Server().url(serverUrl).description(description));
  }
}
