package com.modeunsa.global.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "custom.security")
public class SecurityProperties {

  private boolean permitAll;
  private List<String> permitUrls = new ArrayList<>();
  private OAuth2Properties oauth2 = new OAuth2Properties();
  private CorsProperties cors = new CorsProperties();

  @Getter
  @Setter
  public static class OAuth2Properties {
    private List<String> allowedRedirectDomains = new ArrayList<>();
  }

  @Getter
  @Setter
  public static class CorsProperties {
    private boolean enabled;
    private List<String> allowedOrigins = new ArrayList<>();
  }
}
