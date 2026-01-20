# AES-GCM 암호화 원리

AES-256-GCM 모드를 사용한 대칭키 암호화 알고리즘입니다.

## 개요

- **알고리즘**: AES-256-GCM (Advanced Encryption Standard - Galois/Counter Mode)
- **키 크기**: 256비트 (32바이트)
- **특징**: 암호화 + 인증 통합 제공

## 주요 구성 요소

### 1. AES (Advanced Encryption Standard)

- 대칭키 암호화 알고리즘
- 256비트 키 사용 (AES-256)
- 블록 암호화 방식

### 2. GCM (Galois/Counter Mode)

- **암호화 모드**: CTR 모드 기반
- **인증 태그**: 데이터 무결성 검증
- **병렬 처리**: 성능 최적화

## 암호화 과정

### 1. IV (Initialization Vector) 생성

```java
byte[] iv = new byte[12];  // 12바이트 랜덤 IV 생성
SecureRandom random = new SecureRandom();
random.nextBytes(iv);
```

- **역할**: 동일한 평문도 매번 다른 암호문 생성
- **크기**: 12바이트 (96비트)
- **특징**: 재사용 금지 (매번 새로 생성)

### 2. 암호화 수행

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);  // 128비트 태그
cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
byte[] cipherText = cipher.doFinal(plainTextBytes);
```

- **입력**: 평문 + 비밀키 + IV
- **출력**: 암호문 + 인증 태그 (16바이트)

### 3. IV + 암호문 결합

```java
ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
buffer.put(iv);           // 12바이트 IV
buffer.put(cipherText);   // 암호문 + 태그
```

- **구조**: `[IV(12바이트)][암호문+태그(16바이트)]`
- **이유**: 복호화 시 동일한 IV 필요

### 4. Base64 인코딩

```java
return Base64.getEncoder().encodeToString(encrypted);
```

- **목적**: 바이너리 데이터를 문자열로 변환
- **DB 저장**: VARCHAR 컬럼에 저장 가능

## 복호화 과정

### 1. Base64 디코딩

```java
byte[] encrypted = Base64.getDecoder().decode(encryptedText);
```

### 2. IV 추출

```java
ByteBuffer buffer = ByteBuffer.wrap(encrypted);
byte[] iv = new byte[12];
buffer.get(iv);  // 첫 12바이트가 IV
```

### 3. 암호문 추출

```java
byte[] cipherText = new byte[buffer.remaining()];
buffer.get(cipherText);  // 나머지가 암호문 + 태그
```

### 4. 복호화 및 검증

```java
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
byte[] plainText = cipher.doFinal(cipherText);  // 태그 자동 검증
```

- **태그 검증**: 복호화 시 자동으로 무결성 검증
- **실패 시**: `AEADBadTagException` 발생

## 보안 특징

### 1. 동일 평문 → 다른 암호문

```
평문: "user@example.com"
암호화 1회: "abc123..." (IV: 0x12...)
암호화 2회: "def456..." (IV: 0x34...)  ← 다른 암호문
```

- **IV 랜덤성**: 매번 다른 IV로 패턴 분석 방지

### 2. 인증 태그 (Authentication Tag)

- **크기**: 16바이트 (128비트)
- **역할**: 데이터 변조 감지
- **검증**: 복호화 시 자동 검증

### 3. 무결성 보장

```java
// 데이터 변조 시
catch (AEADBadTagException e) {
    // 복호화 실패 → 데이터 손상 또는 키 불일치
}
```

## 데이터 구조

### 암호화 전 (평문)

```
"user@example.com"  (17바이트)
```

### 암호화 후 (Base64 인코딩 전)

```
[IV(12바이트)][암호문+태그(16바이트 이상)]
총 28바이트 이상
```

### Base64 인코딩 후

```
"dGVzdGl2MTIz..."  (약 38바이트 이상)
```

## 코드 구조

```java
// 암호화
encrypt("user@example.com")
  → IV 생성 (12바이트)
  → AES-GCM 암호화
  → IV + 암호문 결합
  → Base64 인코딩
  → "dGVzdGl2MTIz..."

// 복호화
decrypt("dGVzdGl2MTIz...")
  → Base64 디코딩
  → IV 추출 (12바이트)
  → 암호문 추출
  → AES-GCM 복호화 + 태그 검증
  → "user@example.com"
```

## 주의사항

### 1. IV 재사용 금지

- 동일한 IV + 키 조합은 보안 취약점
- 매번 새로운 랜덤 IV 생성 필수

### 2. 키 관리

- 마스터 키는 안전하게 보관
- 키 유출 시 모든 데이터 복호화 가능

### 3. 태그 검증

- GCM 모드는 자동으로 태그 검증
- 검증 실패 시 예외 발생 (데이터 보호)

## 참고

- **AES**: NIST 표준 대칭키 암호화
- **GCM**: 인증 암호화 모드 (AEAD)
- **IV 크기**: 12바이트 권장 (성능 최적화)
- **태그 크기**: 16바이트 (128비트) 권장
