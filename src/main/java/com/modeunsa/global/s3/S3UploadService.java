package com.modeunsa.global.s3;

import com.modeunsa.global.config.S3Properties;
import com.modeunsa.shared.file.dto.DomainType;
import com.modeunsa.shared.file.dto.PresignedUrlRequest;
import com.modeunsa.shared.file.dto.PresignedUrlResponse;
import com.modeunsa.shared.file.dto.PublicUrlRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
public class S3UploadService {

  private static final String TEMP = "/temp";
  private final String bucket;
  private final String region;
  private final S3Presigner s3Presigner;
  private final S3Client s3Client;

  public S3UploadService(S3Properties s3Properties, S3Presigner s3Presigner, S3Client s3Client) {
    this.bucket = s3Properties.s3().bucket();
    this.region = s3Properties.region().staticRegion();
    this.s3Presigner = s3Presigner;
    this.s3Client = s3Client;
  }

  /** 다운로드용 presignedUrl 발급 public-read 가 아닌 만료시간이 정해져있는 url 입니다 */
  public PresignedUrlResponse getPresignedUrl(String key) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(10)) // TODO: policy 로 옮기기
            .getObjectRequest(getObjectRequest)
            .build();
    String presignedUrl = s3Presigner.presignGetObject(getObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, key);
  }

  /** 업로드용 presignedUrl 발급 */
  public PresignedUrlResponse issuePresignedUrl(PresignedUrlRequest request) {
    String rawKey = TEMP + "/" + request.domainType() + "/" + UUID.randomUUID() + ".png";

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder().bucket(bucket).key(rawKey).contentType("image/png").build();

    PutObjectPresignRequest putObjectPresignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(5))
            .putObjectRequest(putObjectRequest)
            .build();

    String presignedUrl = s3Presigner.presignPutObject(putObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, rawKey);
  }

  /** 업로드 후 public-read 용 url을 생성합니다. */
  public String getPublicUrl(PublicUrlRequest request) {
    // 1. 실제 업로드된 파일 확인
    HeadObjectResponse head =
        s3Client.headObject(
            HeadObjectRequest.builder().bucket(bucket).key(request.rawKey()).build());

    // 2. 검증 (최소 예시)
    if (!head.contentType().startsWith("image/")) {
      throw new IllegalStateException("이미지 파일이 아님");
    }

    String publicKey =
        this.buildPublicKey(request.domainType(), request.domainId(), request.filename());
    // 3. CopyObject로 public-read 전환
    CopyObjectRequest copyRequest =
        CopyObjectRequest.builder()
            .sourceBucket(bucket)
            .sourceKey(request.rawKey())
            .destinationBucket(bucket)
            .destinationKey(publicKey)
            .metadataDirective(MetadataDirective.REPLACE)
            .contentType(request.contentType())
            .build();

    s3Client.copyObject(copyRequest);

    // 4. raw 객체 삭제
    this.delete(request.rawKey());

    return buildPublicUrl(publicKey);
  }

  /** s3에 직접 업로드 (되도록이면 presignedUrl 사용해주세요) */
  public String upload(MultipartFile file, DomainType domainType, Long domainId, String filename)
      throws IOException {
    String publicKey = buildPublicKey(domainType, domainId, filename);
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(publicKey)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

    s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    return buildPublicUrl(publicKey);
  }

  /** s3 컨텐츠 삭제 */
  public void delete(String objectKey) {
    DeleteObjectRequest request =
        DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build();

    s3Client.deleteObject(request);
  }

  private String buildPublicKey(DomainType domainType, Long domainId, String filename) {
    return switch (domainType) {
      case PRODUCT -> "product/" + domainId + "/" + filename;
      case CONTENT -> "content/" + domainId + "/" + filename;
      case MEMBER -> "member/" + domainId + "/" + filename;
    };
  }

  private String buildPublicUrl(String publicKey) {
    return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, publicKey);
  }
}
