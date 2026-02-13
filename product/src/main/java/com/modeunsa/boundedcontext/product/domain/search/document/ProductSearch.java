package com.modeunsa.boundedcontext.product.domain.search.document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
  private String description;

  @Field(type = FieldType.Keyword)
  private String category;

  @Field(type = FieldType.Keyword)
  private String saleStatus;

  @Field(type = FieldType.Scaled_Float)
  private BigDecimal price;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private OffsetDateTime createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private OffsetDateTime updatedAt;

  public ProductSearch(
      String name, String description, String category, String saleStatus, BigDecimal price) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.saleStatus = saleStatus;
    this.price = price;
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }
}
