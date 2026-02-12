package com.modeunsa.global.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.modeunsa.boundedcontext.file.out.s3.S3Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "AWS_ACCESS_KEY", matches = ".+")
@ActiveProfiles("test")
class S3ConfigTest {

  @Autowired private S3Client s3Client;

  @Autowired private S3Properties s3Properties;

  @Test
  void s3Connect() {
    assertDoesNotThrow(
        () -> {
          s3Client.listObjects(builder -> builder.bucket(s3Properties.s3().bucket()).maxKeys(1));
        });
  }
}
