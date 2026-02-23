package com.modeunsa.config;

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
}
