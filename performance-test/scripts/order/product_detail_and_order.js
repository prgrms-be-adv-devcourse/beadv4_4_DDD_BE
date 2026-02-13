import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const MEMBER_ID = __ENV.MEMBER_ID || '2';

export const options = {
  vus: 10,
  duration: '30s',
  thresholds: {
    http_reqs: ['rate>1'],
    http_req_duration: ['p(95)<5000'],
    checks: ['rate>0.90'],
  },
};

export function setup() {
  // 1) Dev 로그인
  const loginUrl = `${BASE_URL}/api/v1/auths/dev/login?memberId=${MEMBER_ID}`;
  const loginRes = http.post(loginUrl, null, {
    headers: { Accept: 'application/json' },
  });

  if (loginRes.status !== 200) {
    throw new Error(`Dev login failed: status=${loginRes.status}, body=${loginRes.body}`);
  }
  let loginJson;
  try {
    loginJson = JSON.parse(loginRes.body);
  } catch (e) {
    throw new Error(`Dev login JSON parse error: ${e.message}`);
  }
  const accessToken = loginJson.accessToken;
  if (!accessToken) {
    throw new Error('Dev login: accessToken missing.');
  }

  // 2) 검색으로 상품 ID 목록 확보 (인증 없이 호출)
  const searchUrl = `${BASE_URL}/api/v1/products/search?page=0&size=50&sort=LATEST&keyword=`;
  const searchRes = http.get(searchUrl, { headers: { Accept: 'application/json' } });
  let productIds = [];
  if (searchRes.status === 200) {
    try {
      const body = JSON.parse(searchRes.body);
      const content = body?.result?.content;
      if (Array.isArray(content) && content.length > 0) {
        productIds = content
          .map((p) => (p.id != null ? Number(p.id) : NaN))
          .filter((n) => Number.isInteger(n) && n > 0);
      }
    } catch (_) {}
  }
  if (productIds.length === 0) {
    const fallback = __ENV.PRODUCT_IDS;
    productIds = fallback
      ? fallback.split(',').map((s) => parseInt(s.trim(), 10)).filter((n) => !Number.isNaN(n))
      : [1, 2, 3];
  }

  return { accessToken, productIds };
}

export default function (data) {
  if (!data?.accessToken || !data?.productIds?.length) {
    console.warn('No accessToken or productIds from setup, skipping iteration');
    sleep(1);
    return;
  }

  const headers = {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    Authorization: `Bearer ${data.accessToken}`,
  };

  const productId = data.productIds[Math.floor(Math.random() * data.productIds.length)];

  // 1) 상품 상세 조회
  const detailRes = http.get(`${BASE_URL}/api/v1/products/${productId}`, { headers });

  check(detailRes, {
    'product detail status is 200': (r) => r.status === 200,
    'product detail has result': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && body.result != null && body.result.id != null;
      } catch (e) {
        return false;
      }
    },
  });

  if (detailRes.status !== 200) {
    console.warn(`product detail failed: id=${productId}, status=${detailRes.status}`);
    sleep(1);
    return;
  }

  // 2) 주문 생성
  const orderBody = JSON.stringify({ productId, quantity: 1 });
  const orderRes = http.post(`${BASE_URL}/api/v1/orders`, orderBody, { headers });

  check(orderRes, {
    'order create status is 200': (r) => r.status === 200,
    'order create has result': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body && body.result != null;
      } catch (e) {
        return false;
      }
    },
  });

  if (orderRes.status !== 200) {
    console.warn(`order create failed: productId=${productId}, status=${orderRes.status}, body=${orderRes.body}`);
  }

  sleep(1);
}
