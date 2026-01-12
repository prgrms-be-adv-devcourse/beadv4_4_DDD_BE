package com.modeunsa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * @author : JAKE
 * @date : 26. 1. 9.
 */
@EnableJpaAuditing
@SpringBootApplication
public class ModeunsaApplication {

  public static void main(String[] args) {
    SpringApplication.run(ModeunsaApplication.class, args);
  }
}
