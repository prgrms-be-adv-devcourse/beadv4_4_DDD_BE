package com.modeunsa.boundedcontext.product.domain.search.document;

import com.modeunsa.boundedcontext.product.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "product_search")
public class ProductSearch {

  @Id private String id;

  @Field(type = FieldType.Text)
  private String name;

  @Field(type = FieldType.Text)
  private String sellerBusinessName;

  @Field(type = FieldType.Text)
  private String description;

  @Field(type = FieldType.Keyword)
  private String category;

  @Field(type = FieldType.Keyword)
  private String saleStatus;

  @Field(type = FieldType.Scaled_Float)
  private BigDecimal salePrice;

  @Field(type = FieldType.Text)
  private String primaryImageUrl;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;

  public ProductSearch(
      String id,
      String name,
      String sellerBusinessName,
      String description,
      String category,
      String saleStatus,
      BigDecimal salePrice,
      String primaryImageUrl,
      Instant createdAt) {
    this.id = id;
    this.name = name;
    this.sellerBusinessName = sellerBusinessName;
    this.description = description;
    this.category = category;
    this.saleStatus = saleStatus;
    this.salePrice = salePrice;
    this.primaryImageUrl = primaryImageUrl;
    this.createdAt = createdAt;
  }

  public static ProductSearch from(Product product) {
    return ProductSearch.create(
        product.getId().toString(),
        product.getName(),
        product.getSeller().getBusinessName(),
        product.getDescription(),
        product.getCategory().name(),
        product.getSaleStatus().name(),
        product.getSalePrice(),
        product.getPrimaryImageUrl(),
        product.getCreatedAt().atZone(ZoneId.of("Asia/Seoul")).toInstant());
  }

  public static ProductSearch create(
      String id,
      String name,
      String sellerBusinessName,
      String description,
      String category,
      String saleStatus,
      BigDecimal salePrice,
      String primaryImageUrl,
      Instant createdAt) {
    return new ProductSearch(
        id,
        name,
        sellerBusinessName,
        description,
        category,
        saleStatus,
        salePrice,
        primaryImageUrl,
        createdAt);
  }
}
