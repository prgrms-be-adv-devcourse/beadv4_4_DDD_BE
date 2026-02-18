package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@SpringBootApplication
public class PaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
