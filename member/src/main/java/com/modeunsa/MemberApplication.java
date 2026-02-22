package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@ConfigurationPropertiesScan
@SpringBootApplication
public class MemberApplication {

  public static void main(String[] args) {
    SpringApplication.run(MemberApplication.class, args);
  }
}
