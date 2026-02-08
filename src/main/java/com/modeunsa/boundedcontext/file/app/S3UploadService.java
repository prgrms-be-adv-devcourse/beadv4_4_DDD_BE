package com.modeunsa.boundedcontext.file.app;

import com.modeunsa.boundedcontext.file.domain.DomainType;
import com.modeunsa.boundedcontext.file.domain.UploadPolicy;
import com.modeunsa.boundedcontext.file.out.s3.S3Properties;
import com.modeunsa.boundedcontext.file.out.s3.S3UploadExecutor;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.file.dto.PresignedUrlRequest;
import com.modeunsa.shared.file.dto.PresignedUrlResponse;
import com.modeunsa.shared.file.dto.PublicUrlRequest;
import com.modeunsa.shared.file.dto.PublicUrlResponse;
import com.modeunsa.shared.file.dto.UploadPathInfo;
import java.io.IOException;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
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
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

// TODO: service 분해 & 이동. usecase 분리 및 s3 의존성 out으로 이동
@Component
public class S3UploadService {

  @Value("${spring.profiles.active:default}")
  private String profile;

  private final String bucket;
  private final String region;
  private final S3Presigner s3Presigner;
  private final S3Client s3Client;
  private final S3UploadExecutor s3UploadExecutor;

  public S3UploadService(
      S3Properties s3Properties,
      S3Presigner s3Presigner,
      S3Client s3Client,
      S3UploadExecutor s3UploadExecutor) {

    this.bucket = s3Properties.s3().bucket();
    this.region = s3Properties.region().staticRegion();
    this.s3Presigner = s3Presigner;
    this.s3Client = s3Client;
    this.s3UploadExecutor = s3UploadExecutor;
  }

  /** 업로드용 presignedUrl 발급 */
  public PresignedUrlResponse issuePresignedUrl(PresignedUrlRequest request) {
    String rawKey = UploadPolicy.buildRawKey(request.domainType(), request.ext(), profile);

    PutObjectRequest putObjectRequest =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(rawKey)
            .contentType(request.contentType())
            .build();

    PutObjectPresignRequest putObjectPresignRequest =
        PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(UploadPolicy.EXPIRATION_TIME))
            .putObjectRequest(putObjectRequest)
            .build();

    String presignedUrl = this.presignPutObject(putObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, rawKey);
  }

  /** 다운로드용 presignedUrl 발급. public-read 가 아닌 만료시간이 정해져있는 url 입니다 */
  public PresignedUrlResponse getPresignedUrl(String key) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(key).build();

    GetObjectPresignRequest getObjectPresignRequest =
        GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(UploadPolicy.EXPIRATION_TIME))
            .getObjectRequest(getObjectRequest)
            .build();
    String presignedUrl = this.presignGetObject(getObjectPresignRequest).url().toString();
    return new PresignedUrlResponse(presignedUrl, key);
  }

  /** 업로드 후 public-read 용 url을 생성합니다. */
  public PublicUrlResponse getPublicUrl(PublicUrlRequest request) {
    // 1. 실제 업로드된 파일 확인
    HeadObjectResponse head = this.headObject(bucket, request.rawKey());

    // 2. 검증
    UploadPathInfo uploadPathInfo = UploadPolicy.parse(request.rawKey());
    this.validateRequest(request, head, uploadPathInfo);

    String publicKey =
        UploadPolicy.buildPublicKey(profile, request.domainType(), uploadPathInfo.filename());

    // 3. CopyObject
    this.copyObject(request.rawKey(), publicKey, request.contentType());

    // 4. raw 객체 삭제
    this.deleteObject(request.rawKey());

    String publicUrl = UploadPolicy.buildPublicUrl(bucket, region, publicKey);
    return new PublicUrlResponse(publicUrl, publicKey);
  }

  /** s3에 직접 업로드 (되도록이면 presignedUrl 사용해주세요) */
  public PublicUrlResponse upload(MultipartFile file, DomainType domainType, String filename)
      throws IOException {
    String publicKey = UploadPolicy.buildPublicKey(profile, domainType, filename);
    PutObjectRequest request =
        PutObjectRequest.builder()
            .bucket(bucket)
            .key(publicKey)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

    this.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

    String publicUrl = UploadPolicy.buildPublicUrl(bucket, region, publicKey);
    return new PublicUrlResponse(publicUrl, publicKey);
  }

  /** s3 컨텐츠 삭제 */
  public void delete(String objectKey) {
    this.deleteObject(objectKey);
  }

  /** 헬퍼 메서드 */
  private PresignedPutObjectRequest presignPutObject(PutObjectPresignRequest request) {
    return s3UploadExecutor.execute(
        "PresignPutObject", () -> s3Presigner.presignPutObject(request));
  }

  private PresignedGetObjectRequest presignGetObject(GetObjectPresignRequest request) {
    return s3UploadExecutor.execute(
        "PresignGetObject", () -> s3Presigner.presignGetObject(request));
  }

  private void putObject(PutObjectRequest request, RequestBody body) {
    s3UploadExecutor.executeVoid("PutObject", () -> s3Client.putObject(request, body));
  }

  private HeadObjectResponse headObject(String bucket, String key) {
    return s3UploadExecutor.execute(
        "HeadObject",
        () -> s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(key).build()));
  }

  private void copyObject(String rawKey, String publicKey, String contentType) {
    s3UploadExecutor.executeVoid(
        "CopyObject",
        () ->
            s3Client.copyObject(
                CopyObjectRequest.builder()
                    .sourceBucket(bucket)
                    .sourceKey(rawKey)
                    .destinationBucket(bucket)
                    .destinationKey(publicKey)
                    .metadataDirective(MetadataDirective.REPLACE)
                    .contentType(contentType)
                    .build()));
  }

  private void deleteObject(String objectKey) {
    s3UploadExecutor.executeVoid(
        "DeleteObject",
        () ->
            s3Client.deleteObject(
                DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build()));
  }

  private void validateRequest(
      PublicUrlRequest request, HeadObjectResponse head, UploadPathInfo uploadPathInfo) {

    if (!UploadPolicy.ALLOWED_CONTENT_TYPES.contains(head.contentType())) {
      throw new GeneralException(ErrorStatus.IMAGE_FILE_EXTENSION_NOT_SUPPORTED);
    }
    if (!uploadPathInfo.profile().equals(profile)
        || !uploadPathInfo.domainType().equals(request.domainType())) {
      throw new GeneralException(ErrorStatus.IMAGE_RAW_KEY_INVALID);
    }
  }
}
