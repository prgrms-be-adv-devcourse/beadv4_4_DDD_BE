package com.modeunsa.global.s3;

import com.modeunsa.global.config.S3Properties;
import com.modeunsa.shared.file.dto.PresignedUrlRequest;
import com.modeunsa.shared.file.dto.PresignedUrlResponse;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3UploadService {
  private final String bucket;
  private final S3Presigner s3Presigner;

  public S3UploadService(S3Properties s3Properties, S3Presigner s3Presigner) {
    this.bucket = s3Properties.s3().bucket();
    this.s3Presigner = s3Presigner;
  }

  // GET
  public PresignedUrlResponse getPreSignedUrl(String key) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10)) // TODO: policy 로 옮기기
            .getObjectRequest(getObjectRequest)
            .build();
    String presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, key);
  }

  // PUT
  public PresignedUrlResponse getPutPreSignedUrl(PresignedUrlRequest request) {
    String objectKey = "file/test/" + UUID.randomUUID() + ".png";
    ;
    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(objectKey)
            //      .contentType("image/png")
            .build();

    PutObjectPresignRequest putObjectPresignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(5))
            .putObjectRequest(putObjectRequest)
            .build();

    String presignedUrl = s3Presigner.presignPutObject(putObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, objectKey);
  }
}
