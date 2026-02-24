# API 단일 대시보드 모니터링 가이드

## 목표

- **목표**: `order`, `member`, `payment`, `api-gateway`, `settlement` 등 여러 API 서비스를 **하나의 대시보드에서 상태/지표를 모니터링**할 수 있게 한다.
- **전제**
  - **인프라**: `k8s/infra` 에 이미 **Grafana**와 **Prometheus**가 Helm 차트로 배포되어 있음 (네임스페이스 `modeunsa`).
  - **앱**: `k8s/app` 에 각 API별 Helm 서브차트가 있음 (api-gateway-api, payment-api, order-api, settlement-api, member-api 등).
  - **Grafana**: **Spring Boot 3.x.x** 대시보드를 import 해서 사용 중인 상태를 전제로 한다.

---

## 현재 구조 요약

| 구분 | 위치 | 비고 |
|------|------|------|
| 인프라 (Prometheus, Grafana) | `k8s/infra` | `./k8s/infra.sh up [dev\|prod]` 로 배포, 네임스페이스 `modeunsa` |
| 앱 (API 서비스들) | `k8s/app` | `./k8s/app.sh up [dev\|prod]` 로 배포, 네임스페이스 `modeunsa` |
| Prometheus 설정 | `k8s/infra/charts/prometheus/values.yaml` | `scrapeConfigs` 로 스크랩 대상 정의 (ConfigMap 기반) |
| 앱별 서비스 이름 | `modeunsa-app-<차트이름>` | 예: `modeunsa-app-payment-api`, `modeunsa-app-order-api` |

- **접속 정보**
  - **Dev**: Prometheus `localhost:30090`, Grafana `localhost:30300`
  - **Prod**: Ingress 기준 Prometheus `<EC2-IP>/prometheus`, Grafana `<EC2-IP>/grafana`

---

## 1. 각 API 서비스의 Spring Boot 설정 (Actuator / Prometheus)

각 API가 `/actuator/prometheus` 를 노출해야 Prometheus가 수집할 수 있다. `payment-service` 의 `application-k3s-prod.yml` 에는 이미 아래와 같은 설정이 있다.

```yaml
management:
  health:
    elasticsearch:
      enabled: false
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: never
    prometheus:
      enabled: true
  prometheus:
    metrics:
      export:
        enabled: true
```

- **할 일**: `order-service`, `member-service`, `settlement-service`, `api-gateway` 등 **다른 서비스**의 프로덕션용 설정 파일(`application-k3s-prod.yml` 또는 사용 중인 프로파일)에도 위 `management` 블록이 있으면 유지, 없으면 **동일하게 추가**한다.
- 보안 설정에서 `/actuator/health`, `/actuator/prometheus` 를 허용 URL에 넣어 두면 된다 (이미 payment 예시에 있음).

---

## 2. Prometheus 스크랩 설정 (앱별로 job 추가)

현재 인프라의 Prometheus는 **ConfigMap**으로 `scrape_configs` 를 관리한다.  
`k8s/infra/charts/prometheus/values.yaml` 에서 **job 단위로** 각 API를 스크랩 대상에 넣으면 된다.

### 2-1. 앱 차트별 서비스 이름과 포트

`k8s/app` 의 서브차트와 `_helpers.tpl` 에 따르면, 배포 후 서비스 이름은 `modeunsa-app-<차트이름>` 이고, 포트는 각 차트의 `values.yaml` 에 정의된 `service.port` 이다.

| 차트 이름 (Chart) | 서비스 이름 (동일 네임스페이스) | 포트 |
|-------------------|--------------------------------|------|
| api-gateway-api   | modeunsa-app-api-gateway-api   | 8080 |
| payment-api       | modeunsa-app-payment-api       | 8083 |
| order-api         | modeunsa-app-order-api         | 8082 |
| settlement-api    | modeunsa-app-settlement-api    | 8084 |
| member-api        | modeunsa-app-member-api        | 8086 |

- 동일 네임스페이스(`modeunsa`) 내에서 스크랩하므로 타깃은 **`서비스이름:포트`** 만 써도 된다 (예: `modeunsa-app-payment-api:8083`).

### 2-2. values.yaml 수정 예시

`k8s/infra/charts/prometheus/values.yaml` 의 `scrapeConfigs` 를 **앱별 job** 으로 나누어 넣는다.  
기존에 `modeunsa-app` 하나만 있던 것을 아래처럼 **API별 job** 으로 교체하면, Grafana에서 **job 이름으로 서비스를 구분**해 볼 수 있다.

```yaml
scrapeInterval: "15s"
scrapeConfigs:
  - job_name: "api-gateway-api"
    metricsPath: "/actuator/prometheus"
    scrapeInterval: "15s"
    scrapeTimeout: "10s"
    targets:
      - "modeunsa-app-api-gateway-api:8080"
  - job_name: "payment-api"
    metricsPath: "/actuator/prometheus"
    scrapeInterval: "15s"
    scrapeTimeout: "10s"
    targets:
      - "modeunsa-app-payment-api:8083"
  - job_name: "order-api"
    metricsPath: "/actuator/prometheus"
    scrapeInterval: "15s"
    scrapeTimeout: "10s"
    targets:
      - "modeunsa-app-order-api:8082"
  - job_name: "settlement-api"
    metricsPath: "/actuator/prometheus"
    scrapeInterval: "15s"
    scrapeTimeout: "10s"
    targets:
      - "modeunsa-app-settlement-api:8084"
  - job_name: "member-api"
    metricsPath: "/actuator/prometheus"
    scrapeInterval: "15s"
    scrapeTimeout: "10s"
    targets:
      - "modeunsa-app-member-api:8086"
```

- 나중에 **product-api**, **file-api**, **inventory-api** 등 차트를 `k8s/app` 에 추가하면, 해당 서비스 이름/포트를 확인한 뒤 위와 같은 형식으로 `scrapeConfigs` 에 job 을 추가하면 된다.
- 수정 후 인프라를 다시 배포해 반영한다: `./k8s/infra.sh up [dev|prod]` (또는 Prometheus Pod만 재시작).

---

## 3. Grafana에서 각 API 상태 보기 (Spring Boot 3.x.x 대시보드 활용)

이미 **Spring Boot 3.x.x** 대시보드를 import 해서 쓰고 있다면, 위처럼 **job_name** 을 API별로 나눠 두면 **같은 대시보드에서 job(서비스)별로** 지표를 나눠 볼 수 있다.

- 대시보드 상단에 **변수**가 있다면:
  - **job** 또는 **instance** 변수를 추가하고, Prometheus에서 오는 `job` 레이블을 선택하도록 하면, 드롭다운으로 **api-gateway-api**, **payment-api**, **order-api** 등을 골라 한 대시보드로 서비스별 상태를 전환해서 볼 수 있다.
- 변수 예시 (Prometheus 데이터 소스 사용 시):
  - 이름: `job`
  - 쿼리: `label_values(up, job)`  
  → 각 API job 이 리스트에 뜨고, 선택한 job 만 대시보드에 반영된다.

이렇게 하면 **하나의 Spring Boot 3.x.x 대시보드**로 **모든 API를 job 선택만으로** 확인할 수 있다.

---

## 4. 체크리스트 정리

| 순서 | 항목 | 위치/액션 |
|------|------|-----------|
| 1 | 각 API에 actuator/prometheus 노출 | 각 서비스 `application-k3s-prod.yml` 등에 `management` 설정 추가 (payment-service 참고) |
| 2 | Prometheus에 앱별 스크랩 job 추가 | `k8s/infra/charts/prometheus/values.yaml` 의 `scrapeConfigs` 를 위 표 기준으로 수정 |
| 3 | 인프라 반영 | `./k8s/infra.sh up [dev|prod]` 로 재배포 또는 Prometheus Pod 재시작 |
| 4 | Grafana에서 서비스 구분 확인 | Spring Boot 3.x.x 대시보드에 `job` 변수 추가 후, API별로 전환해 보기 |

이렇게 하면 **이미 쓰고 있는 infra 의 Grafana/Prometheus**와 **k8s/app 의 앱별 Helm 차트**를 그대로 활용해서, **하나의 대시보드(Spring Boot 3.x.x)에서 각 API별 상태**를 볼 수 있다.
