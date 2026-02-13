package com.modeunsa.boundedcontext.product.app;

import com.modeunsa.boundedcontext.product.in.dto.ProductCursorDto;
import java.time.LocalDateTime;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ProductCursorCodec {
  public String encode(LocalDateTime createdAt, Long productId) {
    String raw = createdAt + "|" + productId;
    return Base64.getEncoder().encodeToString(raw.getBytes());
  }

  public ProductCursorDto decodeIfPresent(String cursor) {
    if (cursor == null) {
      // 첫 조회는 Null 허용
      return null;
    }
    return this.decode(cursor);
  }

  private ProductCursorDto decode(String cursor) {
    String decoded = new String(Base64.getDecoder().decode(cursor));
    String[] parts = decoded.split("\\|");

    return new ProductCursorDto(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
  }
}
