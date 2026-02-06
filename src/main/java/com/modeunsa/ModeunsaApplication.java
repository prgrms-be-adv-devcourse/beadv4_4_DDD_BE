package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@ConfigurationPropertiesScan
@SpringBootApplication
@EnableElasticsearchRepositories
public class ModeunsaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ModeunsaApplication.class, args);
  }
}
