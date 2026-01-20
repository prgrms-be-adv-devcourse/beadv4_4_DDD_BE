package com.modeunsa.global.jpa.converter;

import com.modeunsa.global.encryption.Crypto;
import com.modeunsa.global.encryption.EncryptionException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

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
      throw new EncryptionException("Error occurred during decryption", e);
    }
  }
}
