package com.modeunsa.global.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
class S3ConfigTest {

  @Autowired private S3Client s3Client;

  @Autowired private S3Properties s3Properties;

  @Test
  void s3ConnectTest() {
    assertDoesNotThrow(
        () -> {
          s3Client.listObjects(builder -> builder.bucket(s3Properties.s3().bucket()).maxKeys(1));
        });
  }
}
