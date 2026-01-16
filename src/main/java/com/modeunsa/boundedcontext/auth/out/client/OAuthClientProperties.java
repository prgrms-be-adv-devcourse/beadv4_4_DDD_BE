package com.modeunsa.boundedcontext.auth.out.client;

import java.util.Map;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public record OAuthClientProperties(Map<String, Registration> registration) {
  public record Registration(
      String clientId,
      String clientSecret,
      String clientAuthenticationMethod,
      String authorizationGrantType,
      String clientName,
      Set<String> scope,
      String redirectUri) {}
}
