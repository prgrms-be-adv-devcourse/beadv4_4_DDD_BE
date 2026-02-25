package com.modeunsa.api.pagination;

import com.modeunsa.global.encryption.Crypto;
import com.modeunsa.global.json.JsonConverter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CursorCodec {
  private final Crypto crypto;
  private final JsonConverter jsonConverter;

  public String encode(CursorDto dto) {
    String json = jsonConverter.serialize(dto);
    String encryptedCursor = crypto.encrypt(json);
    return Base64.getEncoder().encodeToString(encryptedCursor.getBytes());
  }

  public <T extends CursorDto> T decodeIfPresent(String cursor, Class<T> type) {
    if (cursor == null) {
      // 첫 조회는 Null 허용
      return null;
    }
    return this.decode(cursor, type);
  }

  private <T extends CursorDto> T decode(String cursor, Class<T> type) {
    String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
    String decryptCursor = crypto.decrypt(decoded);
    return jsonConverter.deserialize(decryptCursor, type);
  }
}
