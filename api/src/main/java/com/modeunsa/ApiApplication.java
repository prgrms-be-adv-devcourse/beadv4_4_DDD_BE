package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@ConfigurationPropertiesScan
@SpringBootApplication(
    exclude = {
      DataElasticsearchAutoConfiguration.class,
      DataElasticsearchRepositoriesAutoConfiguration.class,
      DataElasticsearchReactiveRepositoriesAutoConfiguration.class
    })
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }
}
