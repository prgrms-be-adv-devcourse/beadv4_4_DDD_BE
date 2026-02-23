package com.modeunsa.global.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "custom.security")
@Component
public class SecurityProperties {

  private boolean permitAll;
  private List<String> permitUrls = new ArrayList<>();
}
