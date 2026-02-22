package com.modeunsa.global.jpa.converter;

import com.modeunsa.global.encryption.Crypto;
import com.modeunsa.global.encryption.EncryptionException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
@RequiredArgsConstructor
public class EncryptedStringConverter implements AttributeConverter<String, String> {

  private final Crypto crypto;

  @Override
  public String convertToDatabaseColumn(String attribute) {
    if (attribute == null) {
      return null;
    }

    if (attribute.isEmpty()) {
      return "";
    }

    try {
      return crypto.encrypt(attribute);
    } catch (Exception e) {
      log.error(
          "[암호화 실패] converter={}, valueLength={}, valueSample={}, message={}",
          getClass().getSimpleName(),
          attribute.length(),
          mask(attribute),
          e.getMessage(),
          e);
      throw new EncryptionException("Error occurred during encryption", e);
    }
  }

  @Override
  public String convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }

    if (dbData.isEmpty()) {
      return "";
    }

    try {
      return crypto.decrypt(dbData);
    } catch (Exception e) {
      log.error(
          "[복호화 실패] converter={}, cipherLength={}, cipherSample={}, message={}",
          getClass().getSimpleName(),
          dbData.length(),
          mask(dbData),
          e.getMessage(),
          e);
      throw new EncryptionException("Error occurred during encryption", e);
    }
  }

  private String mask(String value) {
    if (value.length() <= 4) {
      return "****";
    }
    return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
  }
}
