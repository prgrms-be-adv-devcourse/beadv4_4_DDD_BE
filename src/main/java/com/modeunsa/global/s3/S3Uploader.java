package com.modeunsa.global.s3;

import com.modeunsa.global.config.S3Properties;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Component
@RequiredArgsConstructor
public class S3Uploader {

  private final S3Client s3Client;
  private final S3Properties s3Properties;

  public String upload(MultipartFile file, String dirName) {
    if (file == null || file.isEmpty()) {
      throw new GeneralException(ErrorStatus.IMAGE_FILE_REQUIRED);
    }

    // 1. 파일명 중복 방지를 위한 UUID 생성
    String originalFilename = file.getOriginalFilename();
    String fileName = dirName + "/" + UUID.randomUUID() + "_" + originalFilename;

    try {
      // 2. AWS SDK v2를 사용한 업로드 요청 생성
      PutObjectRequest putOb =
          PutObjectRequest.builder()
              .bucket(s3Properties.s3().bucket())
              .key(fileName)
              .contentType(file.getContentType())
              .build();

      // 3. S3에 업로드 실행
      s3Client.putObject(putOb, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

      // 4. 업로드된 파일의 URL 반환
      return s3Client
          .utilities()
          .getUrl(GetUrlRequest.builder().bucket(s3Properties.s3().bucket()).key(fileName).build())
          .toExternalForm();

    } catch (IOException e) {
      throw new GeneralException(ErrorStatus.IMAGE_UPLOAD_FAILED);
    }
  }
}
