package com.modeunsa.global.s3;

import com.modeunsa.global.s3.dto.DomainType;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UploadPolicy {
  public static final String TEMP = "/temp";
  public static final int EXPIRATION_TIME = 5;

  public static String buildRawKey(DomainType domainType, String ext) {
    return TEMP + "/" + domainType + "/" + UUID.randomUUID() + "." + ext;
  }

  public static String buildPublicKey(DomainType domainType, Long domainId, String filename) {
    return switch (domainType) {
      case PRODUCT -> "product/" + domainId + "/" + filename;
      case CONTENT -> "content/" + domainId + "/" + filename;
      case MEMBER -> "member/" + domainId + "/" + filename;
    };
  }

  public static String buildPublicUrl(String bucket, String region, String publicKey) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, publicKey);
  }
}
