# k6 부하 테스트

## 1. 대시보드 보기 (InfluxDB + Grafana)

### 인프라 기동

```bash
make up
# 또는
docker-compose up -d influxdb grafana
```

### 부하 테스트 실행 (메트릭이 InfluxDB로 전송됨)

```bash
make test-payment-member
# 또는
make test-payment-request
```

### Grafana에서 보기

1. 브라우저에서 **http://localhost:3010** 접속 (로그인 없이 사용 가능)
2. **Explore** → 데이터 소스 **InfluxDB** 선택 → 쿼리로 k6 메트릭 확인  
   예: `SELECT * FROM http_req_duration WHERE time > now() - 5m`
3. 또는 **Dashboard** → **Import** → k6 공식 대시보드 ID(**14801**) 입력 후 로드

호스트에서 앱이 떠 있으면:

```bash
TARGET_URL=http://host.docker.internal:8080 make test-payment-member
```

---

## 2. 인프라 없이 터미널만

```bash
make test-payment-request
make test-payment-member
```

메트릭은 터미널 요약으로만 확인됩니다 (InfluxDB/Grafana 미기동 시에도 동작).

---

## 3. 정리

```bash
make down
```
