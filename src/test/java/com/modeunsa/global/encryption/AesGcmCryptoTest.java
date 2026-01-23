package com.modeunsa.global.encryption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("AES Gcm Crypto 테스트")
public class AesGcmCryptoTest {

  private Crypto crypto;
  private String testMasterKey;

  @BeforeEach
  void setUp() {

    byte[] keyBytes = new byte[32];
    new SecureRandom().nextBytes(keyBytes);
    testMasterKey = Base64.getEncoder().encodeToString(keyBytes);

    crypto = new AesGcmCrypto();
    ReflectionTestUtils.setField(crypto, "masterKey", testMasterKey);
    ReflectionTestUtils.setField(crypto, "enabled", true);
    crypto.init();
  }

  @Test
  @DisplayName("AES-GCM 암호화 및 복호화 테스트")
  void encryptAndDecryptTest() {
    // given
    String plaintext = "Sensitive Information";

    // when
    String cipherText = crypto.encrypt(plaintext);
    String decrypted = crypto.decrypt(cipherText);

    // then
    assertThat(decrypted).isEqualTo(plaintext);
    assertThat(cipherText).isNotEqualTo(plaintext);
  }

  @Test
  @DisplayName("동일 평문에 대해 매번 다른 암호문 생성 테스트")
  void encryptSamePlaintextDifferentCiphertextTest() {
    // given
    String plaintext = "test@example.com";

    // when
    String encrypted1 = crypto.encrypt(plaintext);
    String encrypted2 = crypto.encrypt(plaintext);

    // then
    assertThat(encrypted1).isNotEqualTo(encrypted2);
    // 복호화 시 동일한 평문
    assertThat(crypto.decrypt(encrypted1)).isEqualTo(crypto.decrypt(encrypted2));
    assertThat(crypto.decrypt(encrypted1)).isEqualTo(plaintext);
    assertThat(crypto.decrypt(encrypted2)).isEqualTo(plaintext);
  }

  @Test
  @DisplayName("잘못된 암호문 복호화 시 예외 발생 테스트")
  void decryptInvalidCiphertextTest() {
    // given
    String invalidCipherText = "InvalidCipherText";

    // when, then
    assertThatThrownBy(() -> crypto.decrypt(invalidCipherText))
        .isInstanceOf(EncryptionException.class);
  }
}
