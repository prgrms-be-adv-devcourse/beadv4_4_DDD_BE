# 암호화 (AES-GCM) 사용법

민감 정보를 DB에 저장할 때 자동으로 암호화/복호화하는 기능입니다.

## 개요

- **알고리즘**: AES-256-GCM
- **자동 변환**: JPA `AttributeConverter`를 통해 엔티티 ↔ DB 자동 변환
- **암호화 활성화**: `encryption.enabled` 설정으로 제어

## 설정

### application.yml

```yaml
encryption:
  enabled: true
  master-key: ${ENCRYPTION_MASTER_KEY:}
```

### 마스터 키 생성

```bash
# 32바이트(256비트) 랜덤 키 생성 후 Base64 인코딩
openssl rand -base64 32
```

생성된 키를 환경 변수로 설정:
```bash
export ENCRYPTION_MASTER_KEY="생성된_키_값"
```

## 사용법

### 엔티티 필드에 적용

```java
@Entity
public class PaymentMember extends ManualIdAndAuditedEntity {
  
  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false, unique = true)
  private String email;
  
  @Convert(converter = EncryptedStringConverter.class)
  @Column(nullable = false)
  private String name;
}
```

### 동작 방식

1. **저장 시**: 엔티티의 평문 → DB에 암호문 저장
2. **조회 시**: DB의 암호문 → 엔티티의 평문으로 자동 복호화

```java
// 저장
PaymentMember member = PaymentMember.builder()
    .email("user@example.com")  // 평문
    .name("홍길동")              // 평문
    .build();
repository.save(member);
// → DB에는 암호문으로 저장됨

// 조회
PaymentMember found = repository.findById(id);
String email = found.getEmail();  // 자동 복호화되어 평문 반환
```

## 실제 사용 예시

### PaymentMember

```java
@Convert(converter = EncryptedStringConverter.class)
@Column(nullable = false, unique = true)
private String email;

@Convert(converter = EncryptedStringConverter.class)
@Column(nullable = false)
private String name;

@Convert(converter = EncryptedStringConverter.class)
@Column(nullable = false, unique = true)
private String customerKey;
```

### Payment

```java
@Convert(converter = EncryptedStringConverter.class)
private String pgPaymentKey;

@Convert(converter = EncryptedStringConverter.class)
private String pgCustomerName;

@Convert(converter = EncryptedStringConverter.class)
private String pgCustomerEmail;
```

## 주의사항

### 1. 암호화 비활성화 시

```yaml
encryption:
  enabled: false
```

- 평문으로 저장/조회됨
- 기존 암호문 데이터는 복호화 실패 가능

### 2. 마스터 키 변경

- 기존 암호문은 복호화 불가
- 마이그레이션 필요

### 3. DB 컬럼 크기

- 암호문은 평문보다 큼 (IV + 암호문 + Base64 인코딩)
- 충분한 크기로 설정 권장 (예: `VARCHAR(500)`)

### 4. 검색/정렬 불가

- 암호화된 필드는 DB에서 검색/정렬 불가
- 필요 시 별도 인덱스 컬럼 고려

## 예외 처리

```java
try {
    // 암호화/복호화 자동 수행
    paymentRepository.save(payment);
} catch (EncryptionException e) {
    // 마스터 키 오류, 암호문 손상 등
    log.error("암호화 처리 실패", e);
}
```

## 컴포넌트 구조

```
global/
  ├── encryption/
  │   ├── Crypto.java                    # 암호화 인터페이스
  │   ├── AesGcmCrypto.java              # AES-GCM 구현체
  │   └── EncryptionException.java       # 암호화 예외
  └── jpa/
      └── converter/
          └── EncryptedStringConverter.java  # JPA 변환기
```
