package com.modeunsa.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "custom.swagger")
public record SwaggerProperties(String serverUrl, String description) {
  public SwaggerProperties {
    if (serverUrl == null) {
      serverUrl = "http://localhost:8080";
    }
    if (description == null) {
      description = "Default Server";
    }
  }
}
