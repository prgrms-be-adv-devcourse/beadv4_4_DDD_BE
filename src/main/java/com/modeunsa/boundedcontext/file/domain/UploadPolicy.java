package com.modeunsa.boundedcontext.file.domain;

import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.file.dto.UploadPathInfo;
import com.modeunsa.global.status.ErrorStatus;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UploadPolicy {
  public static final String TEMP = "temp";
  public static final int EXPIRATION_TIME = 5;

  public static final Set<String> ALLOWED_CONTENT_TYPES =
      Set.of("image/png", "image/jpeg", "application/pdf");

  public static String buildRawKey(DomainType domainType, String ext, String profile) {
    return String.format(
        "%s/%s/%s/%s.%s",
        profile, TEMP, domainType.toString().toLowerCase(), UUID.randomUUID(), ext);
  }

  public static String buildPublicKey(String profile, DomainType domainType, String filename) {
    return String.format("%s/%s/%s", profile, domainType.toString().toLowerCase(), filename);
  }

  public static String buildPublicUrl(String bucket, String region, String publicKey) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, publicKey);
  }

  public static UploadPathInfo parse(String rawKey) {
    // ex) dev/temp/product/uuid.jpg
    String[] parts = rawKey.split("/");

    if (parts.length < 4) {
      throw new GeneralException(ErrorStatus.IMAGE_RAW_KEY_INVALID);
    }
    String profile = parts[0];
    DomainType domainType = DomainType.valueOf(parts[2].toUpperCase());

    String filename = parts[3];
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex < 0) {
      throw new IllegalArgumentException("Invalid filename: " + filename);
    }

    String uuid = filename.substring(0, dotIndex);
    String extension = filename.substring(dotIndex + 1);

    return new UploadPathInfo(profile, domainType, filename, uuid, extension);
  }
}
