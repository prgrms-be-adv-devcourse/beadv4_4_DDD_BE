package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class FileApplication {

  public static void main(String[] args) {
    SpringApplication.run(FileApplication.class, args);
  }
}
