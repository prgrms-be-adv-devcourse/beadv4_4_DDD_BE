package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchReactiveRepositoriesAutoConfiguration;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchRepositoriesAutoConfiguration;

// @EnableJpaAuditing(auditorAwareRef = "userAuditorAware")
@ConfigurationPropertiesScan
@SpringBootApplication(
    exclude = {
      DataElasticsearchAutoConfiguration.class,
      DataElasticsearchRepositoriesAutoConfiguration.class,
      DataElasticsearchReactiveRepositoriesAutoConfiguration.class
    })
public class ProductApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProductApplication.class, args);
  }
}
