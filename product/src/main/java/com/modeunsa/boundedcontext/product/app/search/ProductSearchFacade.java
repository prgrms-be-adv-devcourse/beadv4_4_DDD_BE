package com.modeunsa.boundedcontext.product.app.search;

import com.modeunsa.api.pagination.CursorCodec;
import com.modeunsa.api.pagination.KeywordCursorDto;
import com.modeunsa.api.pagination.VectorCursorDto;
import com.modeunsa.boundedcontext.product.app.ProductMapper;
import com.modeunsa.boundedcontext.product.domain.search.document.ProductSearch;
import com.modeunsa.boundedcontext.product.in.dto.ProductSliceResultDto;
import com.modeunsa.shared.product.dto.search.ProductSearchRequest;
import com.modeunsa.shared.product.dto.search.ProductSearchResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchFacade {

  private final ProductCreateProductSearchUseCase productCreateProductSearchUseCase;
  private final ProductSearchReindexUseCase productSearchReindexUseCase;
  private final ProductSearchService productSearchService;
  private final ProductMapper productMapper;
  private final CursorCodec cursorCodec;

  @Transactional
  public void createProductSearch(ProductSearchRequest request) {
    productCreateProductSearchUseCase.createProductSearch(request);
  }

  public ProductSliceResultDto search(String keyword, String cursor, int size) {
    // 1. cursor 복호화
    KeywordCursorDto decodedCursor = cursorCodec.decodeIfPresent(cursor, KeywordCursorDto.class);
    // 2. cursor 기반 검색
    Slice<ProductSearch> products =
        productSearchService.keywordSearch(keyword, decodedCursor, size);
    // 3. nextCursor 가져와서 암호화 & 인코딩
    String nextCursor = null;
    if (products.hasNext()) {
      ProductSearch last = products.getContent().getLast();
      nextCursor =
          cursorCodec.encode(
              new KeywordCursorDto<>(last.getCreatedAt(), Long.valueOf(last.getId())));
    }
    return new ProductSliceResultDto(
        products.map(product -> productMapper.toProductSearchResponse(product)), nextCursor);
  }

  public void reindexAll() {
    productSearchReindexUseCase.reindexAll();
  }

  public Page<String> autoComplete(String keyword) {
    return productSearchService.autoComplete(keyword);
  }

  public List<ProductSearchResponse> knnSearch(String keyword) {
    return productSearchService.knnSearch(keyword, 3).stream()
        .map(productMapper::toProductSearchResponse)
        .toList();
  }

  public ProductSliceResultDto hybridSearch(String keyword, String cursor, int size) {
    // 1. cursor 복호화
    VectorCursorDto decodedCursor = cursorCodec.decodeIfPresent(cursor, VectorCursorDto.class);
    // 2. cursor 기반 검색
    Slice<SearchHit<ProductSearch>> products =
        productSearchService.hybridSearch(keyword, decodedCursor, size);
    // 3. nextCursor 가져와서 암호화 & 인코딩
    String nextCursor = null;
    if (products.hasNext()) {
      SearchHit<ProductSearch> lastHit = products.getContent().getLast();
      double lastScore = lastHit.getScore();
      String lastId = lastHit.getContent().getId();
      nextCursor = cursorCodec.encode(new VectorCursorDto(lastScore, lastId));
    }
    Slice<ProductSearchResponse> contents =
        products.map(product -> productMapper.toProductSearchResponse(product.getContent()));
    return new ProductSliceResultDto<>(contents, nextCursor);
  }
}
