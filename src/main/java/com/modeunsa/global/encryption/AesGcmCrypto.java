package com.modeunsa.global.encryption;

import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AesGcmCrypto implements Crypto {

  // AES, GCM 모드
  private static final String ALGORITHM = "AES/GCM/NoPadding";

  // AES-256 사용
  private static final String KEY_ALGORITHM = "AES";
  private static final int KEY_SIZE = 256;

  // IV : (Initialization Vector) : 대칭키 암호화에서 암호문을 매번 달라지게 하는 임의의 값
  // 동일한 평문 (email) 이더라도 매번 암호화할 때마다 암호문이 달라짐
  private static final int GCM_IV_LENGTH = 12;

  // Tag 는 암호문이 변조되었는지 확인하는 역하을 함
  private static final int GCM_TAG_LENGTH = 16;
  private static final int GCM_TAG_LENGTH_BITS = GCM_TAG_LENGTH * 8;

  @Value("${encryption.master-key:}")
  private String masterKey;

  @Value("${encryption.enabled:false}")
  private boolean enabled;

  private SecretKey secretKey;

  // 1. master-key 인 암호화 키를 Base64 디코딩하여 SecretKey 객체 생성
  @PostConstruct
  public void init() {
    if (!enabled) {
      log.warn("암호화가 비활성화되어 있습니다. 민감 정보가 평문으로 저장됩니다.");
      return;
    }

    if (!StringUtils.hasText(masterKey)) {
      throw new EncryptionException(
          "암호화가 활성화되었지만 마스터 키가 설정되지 않았습니다. " + "encryption.master-key 환경 변수를 설정해주세요.");
    }

    try {
      byte[] keyBytes = Base64.getDecoder().decode(masterKey);
      if (keyBytes.length != KEY_SIZE / 8) {
        throw new EncryptionException(
            String.format("마스터 키는 32바이트(256비트)여야 합니다. 현재: %d바이트", keyBytes.length));
      }
      this.secretKey = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    } catch (Exception e) {
      throw new EncryptionException("마스터 키 형식이 올바르지 않습니다.");
    }
  }

  // 매번 encrypt / decrypt 할 때마다 IV(Initialization Vector) 생성
  @Override
  public String encrypt(String plainText) {
    if (!enabled) {
      return plainText;
    }

    if (!StringUtils.hasText(plainText)) {
      return plainText;
    }

    try {
      // 1. IV(Initialization Vector) 생성
      byte[] iv = new byte[GCM_IV_LENGTH];
      SecureRandom random = new SecureRandom();
      random.nextBytes(iv);

      // 2. Cipher 초기화, AES-256 GCM 모드로 암호화 설정
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

      // 3. 암호화 수행
      byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
      byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);

      // 4. IV + 암호문을 결합하여 저장
      ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + cipherTextBytes.length);
      byteBuffer.put(iv);
      byteBuffer.put(cipherTextBytes);
      byte[] encrypted = byteBuffer.array();

      // 5. Base64 인코딩
      return Base64.getEncoder().encodeToString(encrypted);
    } catch (Exception e) {
      throw new EncryptionException("암호화 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public String decrypt(String encryptedText) {
    if (!enabled) {
      return encryptedText;
    }

    if (!StringUtils.hasText(encryptedText)) {
      return encryptedText;
    }

    try {

      // 1. Base64 디코딩
      byte[] encrypted = Base64.getDecoder().decode(encryptedText);

      // 2. IV 추출
      if (encrypted.length < GCM_IV_LENGTH) {
        throw new EncryptionException(
            String.format(
                "암호문 길이가 너무 짧습니다. 최소 %d바이트 필요, 현재: %d바이트", GCM_IV_LENGTH, encrypted.length));
      }

      ByteBuffer byteBuffer = ByteBuffer.wrap(encrypted);
      // IV 추출 (첫 12바이트)
      byte[] iv = new byte[GCM_IV_LENGTH];
      byteBuffer.get(iv);

      // 3. 암호문 추출
      byte[] ciphertextBytes = new byte[byteBuffer.remaining()];
      byteBuffer.get(ciphertextBytes);

      // 4. Cipher 초기화
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      // 5. 복호화 수행, 태그 검증 포함
      byte[] plainTextBytes = cipher.doFinal(ciphertextBytes);
      return new String(plainTextBytes, StandardCharsets.UTF_8);
    } catch (IllegalArgumentException ie) {
      throw new EncryptionException("암호문 형식이 올바르지 않습니다.", ie);
    } catch (AEADBadTagException ae) {
      throw new EncryptionException("복호화 인증 실패. 키가 올바르지 않거나 데이터가 손상되었습니다.", ae);
    } catch (Exception e) {
      throw new EncryptionException("복호화 중 오류가 발생했습니다.", e);
    }
  }
}
