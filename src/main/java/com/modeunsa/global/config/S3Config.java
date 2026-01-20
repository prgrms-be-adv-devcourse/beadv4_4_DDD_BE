package com.modeunsa.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
@RequiredArgsConstructor
public class S3Config {

  private final S3Properties s3Properties;

  @Bean
  public S3Client s3Client() {
    AwsBasicCredentials credentials =
        AwsBasicCredentials.create(
            s3Properties.credentials().accessKey(), s3Properties.credentials().secretKey());

    return S3Client.builder()
        .region(Region.of(s3Properties.region().staticRegion()))
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .build();
  }
}
