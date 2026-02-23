# 결제 목록 조회 쿼리 인덱스 튜닝

결제 목록 API(`GET /api/v2/payments`)의 회원별 결제 검색 쿼리 성능 개선을 위한 인덱스 도입 및 실행 계획 분석 정리.

---

## 1. 대상 쿼리

```sql
EXPLAIN SELECT
    p1_0.order_no,
    p1_0.order_id,
    p1_0.status,
    p1_0.total_amount,
    p1_0.pg_order_name,
    p1_0.payment_provider,
    p1_0.created_at
FROM payment_payment p1_0
WHERE
    p1_0.member_id = 10
    AND p1_0.created_at BETWEEN '2025-01-01 23:24:01' AND '2026-02-23 23:24:01'
ORDER BY p1_0.created_at DESC;
```

- **조건**: `member_id` + `created_at` 기간
- **정렬**: `created_at DESC`

---

## 2. EXPLAIN 결과 해석 (참고)

| 항목 | 설명 |
|------|------|
| **id** | 복잡한 쿼리에서의 실행 순서 |
| **table** | 처리 중인 테이블 |
| **type** | 데이터 조회 방식 (ref, range 등) |
| **possible_keys** | 사용 가능한 인덱스 후보 |
| **key** | 실제 사용한 인덱스 |
| **ref** | 조인/조회 기준 값 |
| **rows** | 접근한 행 수(추정). 튜닝 시 줄이는 것이 목표 |
| **filtered** | WHERE 등 필터 후 남는 비율(추정) |
| **Extra** | `Using where`, `Using filesort` 등 부가 정보 |

※ `actual time` 등은 DB 엔진 내부 연산 시간만 포함하며, 네트워크/클라이언트 후처리 시간은 포함되지 않음.

---

## 3. AS-IS: 인덱스 추가 전

### 3.1 실행 계획 (트리)

```
-> Sort: p1_0.created_at DESC  (cost=29.9 rows=2314) (actual time=27.6..28.1 rows=5013 loops=1)
    -> Filter: (p1_0.created_at between '2025-01-01 23:24:01.515189' and '2026-02-23 23:24:01.515189')  (cost=29.9 rows=2314) (actual time=0.0737..12.1 rows=5013 loops=1)
        -> Index lookup on p1_0 using PRIMARY (member_id=10)  (cost=29.9 rows=2314) (actual time=0.058..8.67 rows=5013 loops=1)
```

### 3.2 테이블 형식

| id | select_type | table | type | possible_keys | key    | key_len | ref   | rows | filtered | Extra                      |
|----|-------------|-------|------|---------------|--------|---------|-------|------|----------|----------------------------|
| 1  | SIMPLE      | p1_0  | ref  | PRIMARY       | PRIMARY| 8       | const | 508  | 11.11    | Using where; Using filesort|

### 3.3 동작 요약

1. **PK (member_id, order_no)** 로 `member_id = 10` 인 행만 읽음 → 약 1013~5013행 (데이터 증가에 따라 변동).
2. **Filter**: `created_at` BETWEEN 조건은 이미 읽은 행에 대해 체크만 하고, 행 수를 줄이지 못함.
3. **Sort**: `created_at DESC` 정렬을 위해 **filesort** 수행 → 약 5~28ms 수준.

즉, **PK로 member_id만 활용**하고, 기간 조건과 정렬은 인덱스 없이 **테이블/메모리에서 처리**되는 구조.

---

## 4. TO-BE: 인덱스 추가 후

### 4.1 추가한 인덱스

```sql
-- member_id + created_at DESC 복합 인덱스
CREATE INDEX idx_payment_member_created_at
ON payment_payment (member_id, created_at DESC);
```

### 4.2 실행 계획 (트리)

```
-> Index range scan on p1_0 using idx_payment_member_created_at
   over (member_id = 10 AND '2026-02-23 23:24:01.515189' <= created_at <= '2025-01-01 23:24:01.515189'),
   with index condition: ((p1_0.member_id = 10) and (p1_0.created_at between '2025-01-01 23:24:01.515189' and '2026-02-23 23:24:01.515189'))
   (cost=2120 rows=2314) (actual time=1.14..16.6 rows=5013 loops=1)
```

### 4.3 동작 요약

- **PRIMARY 대신** `idx_payment_member_created_at` 사용.
- **Index range scan**: `member_id = 10` + `created_at` BETWEEN 구간을 **인덱스에서만** 범위 스캔.
- **ORDER BY created_at DESC** 가 인덱스 정렬과 일치해 **filesort 제거** (또는 크게 감소).
- Query cost 약 **235.58** 수준으로, 정렬 비용이 인덱스 스캔으로 흡수됨.

---

## 5. AS-IS vs TO-BE

| 구분 | AS-IS | TO-BE |
|------|--------|--------|
| **인덱스** | PRIMARY (member_id, order_no) | idx_payment_member_created_at (member_id, created_at DESC) |
| **접근 방식** | Index lookup on PRIMARY → Filter → **Sort (filesort)** | **Index range scan** (member_id + created_at) → ORDER |
| **정렬** | 메모리/디스크 filesort | 인덱스 순서 활용 (filesort 제거·감소) |
| **소요 시간** | 5~28ms 수준 (데이터량에 따라 증가) | 인덱스 스캔 위주로 안정화 |

---

## 6. 정리

- 결제 목록은 **member_id + created_at 기간 + created_at DESC 정렬** 패턴이므로, **member_id, created_at DESC** 복합 인덱스가 적합함.
- 이 인덱스로 **범위 스캔 + 정렬**을 인덱스에서 처리해 filesort를 줄이고, 목록 조회 지연을 완화함.
- 인덱스는 마이그레이션(Flyway/Liquibase 등) 또는 수동 DDL로 적용하고, JPA `@Table(indexes = ...)` 는 문서화 목적으로만 둘 수 있음 (DESC는 JPA 스펙에서 지원하지 않음).
