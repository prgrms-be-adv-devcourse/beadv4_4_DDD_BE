import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';

// SCENARIO:
// - 'staged'   (기본)   : 가변 부하(5→10→20→30 VU)
// - 'constant'          : 고정 부하(VU 고정, closed-loop)
// - 'arrival'           : 고정 RPS(도착률 기반), constant-arrival-rate 사용
// - 'step'              : 단계적 RPS/VU 증가 시나리오(STEP_OPTIONS 사용)
//
// TPS: 실행 후 요약에 나오는 http_reqs 의 rate 값 = 초당 요청 수 = TPS
const SCENARIO = __ENV.SCENARIO || 'staged';
const ARRIVAL_RATE = Number(__ENV.ARRIVAL_RATE || '50');      // 초당 요청 수(target RPS)
const ARRIVAL_DURATION = __ENV.ARRIVAL_DURATION || '5m';      // 테스트 지속 시간

// 고정 VU 시나리오(150 RPS 목표용)
// 응답 ~200ms + sleep 1s 기준: VU ≈ 150 × (0.2 + 1) ≈ 180
const CONSTANT_VUS = Number(__ENV.CONSTANT_VUS || '180');
const CONSTANT_DURATION = __ENV.CONSTANT_DURATION || '5m';

// 검색에 사용할 키워드 풀 (가상 사용자별로 다양한 검색 시뮬레이션)
const KEYWORDS = [
  '가방', '신발', '티셔츠', '바지', '코트',
  '노트북', '키보드', '마우스', '모니터',
  '책', '음악', '스포츠', '여행', '식품',
];

// GET /api/v1/products/search: page, size 필수. sort는 LATEST | PRICE_ASC | PRICE_DESC
const PAGE_SIZE = 20;
const SORT_TYPES = ['LATEST', 'PRICE_ASC', 'PRICE_DESC'];

// 단계적 부하: VU를 서서히 올렸다 유지했다 내림. TPS는 http_reqs rate로 확인.
const STAGED_OPTIONS = {
  stages: [
    { duration: '30s', target: 5 },   // 0 → 5 VU (워밍업)
    { duration: '30s', target: 10 },  // 5 → 10 VU
    { duration: '1m', target: 10 },   // 10 VU 유지
    { duration: '30s', target: 20 },  // 10 → 20 VU
    { duration: '1m', target: 20 },   // 20 VU 유지
    { duration: '30s', target: 30 },  // 20 → 30 VU
    { duration: '1m', target: 30 },   // 30 VU 유지
    { duration: '30s', target: 0 },   // 램프 다운
  ],
  thresholds: {
    http_reqs: ['rate>1'],              // TPS: 초당 요청 수가 1보다 커야 함
    http_req_duration: ['p(95)<5000'], // p95 응답시간 5초 미만
    checks: ['rate>0.90'],              // 90% 이상 체크 통과 (일시적 오류 허용)
  },
};

// 고정 부하 (closed-loop, VU 고정)
const CONSTANT_OPTIONS = {
  vus: CONSTANT_VUS,
  duration: CONSTANT_DURATION,
  thresholds: {
    http_reqs: ['rate>1'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.90'],
  },
};

// 고정 RPS(Arrival-rate) 시나리오: 초당 ARRIVAL_RATE 만큼 요청을 발생시키는 executor
const ARRIVAL_OPTIONS = {
  scenarios: {
    product_search_arrival: {
      executor: 'constant-arrival-rate',
      rate: ARRIVAL_RATE,          // RPS
      timeUnit: '1s',
      duration: ARRIVAL_DURATION,
      // k6 자체 병목 방지를 위해 RPS 대비 충분한 VU 수 확보
      preAllocatedVUs: Math.max(Math.ceil(ARRIVAL_RATE / 2), 50),
      maxVUs: Math.max(ARRIVAL_RATE * 2, 200),
    },
  },
  thresholds: {
    dropped_iterations: ['rate<0.01'],
    http_reqs: ['rate>1'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.90'],
  },
};

// STEP 시나리오: 목표 RPS 기준으로 VU를 단계적으로 올리는 부하 (대략 RPS 기반)
// 가정: 평균 응답시간 ~200ms + sleep 1s => 1.2초당 1회, VU ≈ RPS × 1.2
const STEP_TARGET_RPS = Number(__ENV.TARGET_RPS || '200');
const STEP_FINAL_VUS = Math.max(Math.ceil(STEP_TARGET_RPS * 1.2), 1);
const STEP_OPTIONS = {
  stages: [
    { duration: '1m', target: Math.ceil(STEP_FINAL_VUS * 0.25) }, // ~25% RPS
    { duration: '1m', target: Math.ceil(STEP_FINAL_VUS * 0.5) },  // ~50% RPS
    { duration: '1m', target: Math.ceil(STEP_FINAL_VUS * 0.75) }, // ~75% RPS
    { duration: '2m', target: STEP_FINAL_VUS },                    // ~100% RPS
    { duration: '30s', target: 0 },                                // 램프 다운
  ],
  thresholds: {
    http_reqs: ['rate>1'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.90'],
  },
};

export const options =
    SCENARIO === 'constant'
        ? CONSTANT_OPTIONS
        : SCENARIO === 'arrival'
            ? ARRIVAL_OPTIONS
            : SCENARIO === 'step'
                ? STEP_OPTIONS
                : STAGED_OPTIONS;

export default function () {
  const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
  const page = Math.floor(Math.random() * 5); // 0~4 페이지
  const sort = SORT_TYPES[Math.floor(Math.random() * SORT_TYPES.length)];
  const query = `page=${page}&size=${PAGE_SIZE}&sort=${sort}&keyword=${encodeURIComponent(keyword)}`;
  const url = `${BASE_URL}/api/v1/products/search?${query}`;

  const res = http.get(url, {
    headers: { Accept: 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response has result': (r) => {
      try {
        const body = JSON.parse(r.body);
        if (!body || body.result == null) return false;
        // Page 응답: result.content 배열 또는 result 자체가 배열/객체
        const c = body.result.content;
        return c === undefined || c === null || Array.isArray(c);
      } catch (e) {
        return false;
      }
    },
  });

  if (res.status !== 200) {
    console.warn(`search failed: keyword=${keyword}, status=${res.status}, body=${res.body}`);
  }
  // Arrival-rate 시나리오에서는 think time 최소화
  if (SCENARIO === 'arrival') {
    sleep(0);
  } else {
    sleep(1);
  }
}