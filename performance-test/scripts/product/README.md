# 상품 검색 API 부하 테스트 (product_search.js)

`GET /api/v1/products/search` (keyword, page, size, sort) 에 대한 **단계적 부하** 테스트와 **TPS** 측정 가이드입니다.

- **엔드포인트**: `GET /api/v1/products/search`
- **쿼리 파라미터**: `keyword`(선택), `page`(필수), `size`(필수), `sort`(선택: LATEST, PRICE_ASC, PRICE_DESC)
- **응답**: ApiResponse + Page&lt;ProductResponse&gt; (result.content 배열)

---

## 1. TPS 확인 방법

k6 실행 후 나오는 요약에서 **TPS**는 다음 값으로 확인합니다.

- **`http_reqs`**  
  - **count**: 총 요청 수  
  - **rate**: 초당 요청 수 = **TPS** (Transactions Per Second)

예시:
```
http_reqs......................: 1234   12.5/s
```
→ TPS = **12.5** (초당 12.5건)

추가로 보면 좋은 지표:
- **`http_req_duration`**: 응답 시간 (avg, p95, max)
- **`iterations`** / **`iteration_duration`**: VU당 반복 횟수·소요 시간
- **`checks`**: 검증 통과 비율 (status 200, result.content 존재 등)

---

## 2. 시나리오: 단계적 부하 vs 고정 부하 vs 고정 RPS

| 환경변수 | 설명 |
|----------|------|
| `SCENARIO=staged` (기본) | VU를 5 → 10 → 20 → 30 으로 올렸다가 내리는 **단계적 부하**. 각 단계별 TPS·지연 구간 확인용. |
| `SCENARIO=constant` | VU 10명, 30초 **고정 부하**. 단순 스모크/회귀용. |
| `SCENARIO=arrival` | k6의 `constant-arrival-rate` executor로 **고정 RPS**(초당 요청 수) 부하. 서버가 버틸 수 있는 최대 RPS 탐색용. |

예시:
```bash
# 단계적 부하 (기본)
k6 run scripts/product/product_search.js

# 고정 부하
SCENARIO=constant k6 run scripts/product/product_search.js

# 고정 RPS (예: 초당 100 요청, 1분)
SCENARIO=arrival ARRIVAL_RATE=100 ARRIVAL_DURATION=1m k6 run scripts/product/product_search.js
```

---

## 3. 데이터셋 단계와 테스트 순서

**전제:** v1 검색은 DB(상품 테이블) 기준이므로, 상품 데이터 수를 단계적으로 늘리면서 같은 스크립트로 부하를 걸어 성능을 비교합니다.

### 3.1 데이터 규모 단계 (권장)

| 단계 | 데이터 수 | 비고 |
|------|-----------|------|
| 1 | 10 | 초기 스모크, 연동 확인 |
| 2 | 100 | 소규모 |
| 3 | 1,000 | 중소 규모 |
| 4 | 10,000 | 중규모 |
| 5 | 100,000+ | 대규모 (선택) |

각 단계에서 **동일한 검색 API**를 쓰고, **동일한 k6 옵션**(staged 또는 constant)으로 돌려서 TPS·지연을 비교합니다.

### 3.2 단계별 테스트 수행 순서

1. **데이터 준비**  
   - 해당 단계의 데이터 수만큼 상품을 DB에 적재 (상품 생성 API 또는 시드 데이터로 준비)

2. **환경 확인**  
   - 상품 API 서버 기동  
   - `TARGET_URL`이 해당 서버를 가리키는지 확인

3. **단계적 부하 실행 (TPS·지연 확인)**  
   ```bash
   TARGET_URL=http://localhost:8080 k6 run scripts/product/product_search.js
   ```  
   - 출력 요약에서 **`http_reqs` rate = TPS**  
   - **`http_req_duration`** (avg, p95, max) 기록

4. **결과 기록**  
   - 데이터 수, TPS, p95, max, 실패율(checks) 등을 표로 정리해 두면 이후 단계와 비교하기 좋습니다.

5. **다음 데이터 규모로 반복**  
   - 데이터를 100 → 1,000 → 10,000 등으로 늘린 뒤 2~4 반복

### 3.3 예시 실행 (데이터 10개 → 100개 → 1,000개)

```bash
cd performance-test

# 1) 데이터 10개 상태에서 단계적 부하
TARGET_URL=http://localhost:8080 k6 run scripts/product/product_search.js
# → TPS, p95 기록

# 2) 데이터 100개로 늘린 뒤 동일 테스트
TARGET_URL=http://localhost:8080 k6 run scripts/product/product_search.js
# → TPS, p95 기록 및 10개와 비교

# 3) 데이터 1,000개로 늘린 뒤 동일 테스트
TARGET_URL=http://localhost:8080 k6 run scripts/product/product_search.js
# → TPS, p95 기록 및 이전 단계와 비교
```

Docker Compose로 돌리는 경우:
```bash
TARGET_URL=http://host.docker.internal:8080 make test-product-search
```

---

## 4. 결과 기록 예시 표

| 데이터 수 | TPS (http_reqs rate) | p95 (ms) | max (ms) | checks rate |
|-----------|----------------------|----------|----------|-------------|
| 10        | (측정값)             | (측정값) | (측정값) | (측정값)    |
| 100       | (측정값)             | (측정값) | (측정값) | (측정값)    |
| 1,000     | (측정값)             | (측정값) | (측정값) | (측정값)    |
| 10,000    | (측정값)             | (측정값) | (측정값) | (측정값)    |

데이터가 늘어날수록 TPS 하락·지연 증가 구간을 보면 스케일 시 병목을 판단할 수 있습니다.

---

## 5. 스크립트 옵션 요약

| 환경변수          | 기본값                 | 설명 |
|-------------------|------------------------|------|
| `TARGET_URL`      | http://localhost:8080  | 상품 API 베이스 URL |
| `SCENARIO`        | staged                 | `staged` = 단계적 부하, `constant` = 고정 부하, `arrival` = 고정 RPS 부하 |
| `ARRIVAL_RATE`    | 50                     | `SCENARIO=arrival`일 때 목표 RPS (초당 요청 수) |
| `ARRIVAL_DURATION`| 5m                     | `SCENARIO=arrival`일 때 테스트 지속 시간 |

스크립트 동작:
- `keyword`: KEYWORDS 배열에서 랜덤 선택 (빈 값이면 키워드 없이 검색)
- `page`: 0~4 랜덤
- `size`: 20 고정
- `sort`: LATEST, PRICE_ASC, PRICE_DESC 중 랜덤

단계적 부하 구간 (staged):
- 0 → 5 VU (30s), 5 → 10 VU (30s), 10 VU 유지 (1m)
- 10 → 20 VU (30s), 20 VU 유지 (1m)
- 20 → 30 VU (30s), 30 VU 유지 (1m), 30 → 0 (30s)

Thresholds:
- `http_reqs`: rate > 1 (TPS 최소 1)
- `http_req_duration`: p95 < 5000 ms
- `checks`: rate > 0.90
- `dropped_iterations`: rate < 0.01  (`SCENARIO=arrival`일 때 드롭 비율이 1% 미만인지 확인)

필요하면 `scripts/product/product_search.js` 안의 `stages` / `thresholds` 를 수정해 부하 구간이나 기준을 조정하면 됩니다.
