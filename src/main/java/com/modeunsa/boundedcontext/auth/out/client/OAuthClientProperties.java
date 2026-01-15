package com.modeunsa.boundedcontext.auth.out.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthClientProperties {

  private Map<String, Registration> registration = new HashMap<>();

  @Getter
  @Setter
  public static class Registration {
    private String clientId;
    private String redirectUri;
  }
}