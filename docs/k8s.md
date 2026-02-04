# Kubernetes & Helm 인프라 가이드

이 문서는 Modeunsa 프로젝트의 Kubernetes 인프라 구성과 사용법을 설명합니다.

## 목차

1. [사전 준비](#사전-준비)
2. [빠른 시작](#빠른-시작)
3. [인프라 구성 요소](#인프라-구성-요소)
4. [Helm Chart 구조](#helm-chart-구조)
5. [서브차트와 Values 전달 방식](#서브차트와-values-전달-방식)
6. [명령어 가이드](#명령어-가이드)
7. [Helm 템플릿 문법](#helm-템플릿-문법)
8. [트러블슈팅](#트러블슈팅)

---

## 사전 준비

### 1. Colima 및 Docker CLI 설치

```bash
brew install colima docker
```

> Colima가 Docker 런타임 + k3s를 모두 제공하므로 Docker Desktop은 불필요합니다.

### 2. k3s 모드로 Colima 시작

```bash
colima start --kubernetes --kubernetes-distribution k3s \
  --cpu 4 --memory 6 --disk 30
```

### 3. kubectl, Helm 설치

```bash
brew install kubectl helm
```

---

## 빠른 시작

### 인프라 시작

```bash
./k8s/infra.sh up
```

### 인프라 중지 (데이터 유지)

```bash
./k8s/infra.sh down
```

### 인프라 완전 삭제 (데이터 포함)

```bash
./k8s/infra.sh clean
```

### 접속 정보

| 서비스        | 접속 주소              | 접속 방식           |
|--------------|----------------------|--------------------|
| MySQL        | `localhost:30306`    | Traefik NodePort    |
| Redis        | `localhost:30379`    | Traefik NodePort    |
| Elasticsearch| `localhost:30920`    | Traefik NodePort    |
| Prometheus   | `localhost:30090`    | NodePort            |
| Grafana      | `localhost:30300`    | NodePort            |

> MySQL, Redis, Elasticsearch는 Traefik의 IngressRouteTCP를 통해 TCP 라우팅됩니다.
> Traefik이 각 서비스의 entrypoint(포트)를 받아 해당 백엔드 서비스로 전달합니다.

---

## 인프라 구성 요소

### MySQL (StatefulSet)

- **이미지**: `mysql:8.0`
- **용도**: 메인 데이터베이스
- **스토리지**: 5Gi PVC
- **리소스**: CPU 100m~200m, Memory 256Mi~512Mi

### Redis (StatefulSet)

- **이미지**: `redis:7-alpine`
- **용도**: 캐시, 세션 저장
- **스토리지**: 1Gi PVC
- **리소스**: CPU 50m~250m, Memory 64Mi~256Mi

### Elasticsearch (StatefulSet)

- **이미지**: `elasticsearch:9.2.2`
- **용도**: 검색 엔진 (Nori 플러그인 포함)
- **스토리지**: 5Gi PVC
- **리소스**: CPU 250m~1, Memory 1Gi~2Gi

### Prometheus (Deployment)

- **이미지**: `prom/prometheus:latest`
- **용도**: 메트릭 수집 및 모니터링
- **접속**: NodePort 30090
- **리소스**: CPU 500m, Memory 512Mi

### Grafana (Deployment)

- **이미지**: `grafana/grafana:latest`
- **용도**: 대시보드 시각화
- **접속**: NodePort 30300
- **스토리지**: 2Gi PVC
- **리소스**: CPU 500m, Memory 512Mi

---

## Helm Chart 구조

서브차트(subchart) 구조를 사용하여 각 인프라 서비스를 독립적인 차트로 관리합니다.

```
k8s/
├── infra.sh                          # 인프라 관리 스크립트
└── infra/                            # 부모 Helm Chart
    ├── Chart.yaml                    # 부모 Chart 메타데이터
    ├── values.yaml                   # 서브차트에 전달할 값
    └── charts/                       # 서브차트 디렉토리
        ├── mysql/
        │   ├── Chart.yaml
        │   ├── values.yaml           # 기본값
        │   └── templates/
        │       ├── _helpers.tpl
        │       ├── mysql-secret.yaml
        │       ├── mysql-pvc.yaml
        │       ├── mysql-statefulset.yaml
        │       └── mysql-service.yaml
        ├── redis/
        │   ├── Chart.yaml
        │   ├── values.yaml
        │   └── templates/
        │       ├── _helpers.tpl
        │       ├── redis-secret.yaml
        │       ├── redis-pvc.yaml
        │       ├── redis-statefulset.yaml
        │       └── redis-service.yaml
        ├── elasticsearch/
        │   ├── Chart.yaml
        │   ├── values.yaml
        │   └── templates/
        │       ├── _helpers.tpl
        │       ├── elasticsearch-pvc.yaml
        │       ├── elasticsearch-statefulset.yaml
        │       └── elasticsearch-service.yaml
        ├── prometheus/
        │   ├── Chart.yaml
        │   ├── values.yaml
        │   └── templates/
        │       ├── _helpers.tpl
        │       ├── prometheus-configmap.yaml
        │       ├── prometheus-deployment.yaml
        │       └── prometheus-service.yaml
        ├── grafana/
        │   ├── Chart.yaml
        │   ├── values.yaml
        │   └── templates/
        │       ├── _helpers.tpl
        │       ├── grafana-secret.yaml
        │       ├── grafana-pvc.yaml
        │       ├── grafana-deployment.yaml
        │       └── grafana-service.yaml
        └── traefik/
            ├── Chart.yaml
            ├── values.yaml
            └── templates/
                ├── _helpers.tpl
                ├── traefik-crd.yaml
                ├── traefik-rbac.yaml
                ├── traefik-deployment.yaml
                ├── traefik-service.yaml
                └── traefik-ingressroute.yaml
```

### Chart.yaml

부모 Chart의 메타데이터를 정의합니다. `charts/` 디렉토리의 서브차트는 Helm이 자동으로 인식합니다.

```yaml
apiVersion: v2
name: modeunsa-infra
description: Modeunsa Infrastructure
type: application
version: 0.1.0
```

### 서브차트와 Values 전달 방식

각 서비스는 독립적인 서브차트로 구성됩니다. 서브차트는 자체 `values.yaml`에 기본값을 갖고 있으며,
부모의 `values.yaml`에서 서브차트 이름을 키로 사용하여 값을 전달(덮어쓰기)합니다.

부모 `values.yaml` (서브차트에 전달할 값):
```yaml
mysql:
  enabled: true
  image:
    tag: "8.0"
  auth:
    rootPassword: ""   # .env에서 주입
    database: ""       # .env에서 주입
  storage:
    size: 5Gi

redis:
  enabled: true
  image:
    tag: "7-alpine"
  auth:
    password: ""       # .env에서 주입
  # ...
```

서브차트 `charts/mysql/values.yaml` (기본값):
```yaml
enabled: true
image:
  tag: "8.0"
auth:
  rootPassword: ""
  database: ""
storage:
  size: 5Gi
```

서브차트 내부 템플릿에서는 **서브차트 이름 없이** 값을 참조합니다:

```yaml
# 부모 차트 방식 (이전): {{ .Values.mysql.image.tag }}
# 서브차트 방식 (현재): {{ .Values.image.tag }}
```

#### Values 우선순위 (낮은 순 → 높은 순)

```
1. 서브차트의 values.yaml        (기본값, 가장 낮음)
2. 부모의 values.yaml            (서브차트 이름 키로 전달)
3. --set 플래그                   (가장 높음, 최종 덮어쓰기)
```

#### 값 전달 예시

```bash
helm upgrade --install modeunsa-infra ./k8s/infra -n modeunsa \
  --set mysql.auth.rootPassword="mypassword"
```

Helm 내부에서의 값 병합:

```
서브차트 기본값      →  mysql/values.yaml: { image: { tag: "8.0" }, auth: { rootPassword: "" } }
부모 values.yaml    →  mysql: { image: { tag: "8.0" }, auth: { rootPassword: "" } }
────────────────────────────────────────────────────────────
+ --set mysql.auth.rootPassword="mypassword"  →  최종 덮어쓰기
```

`--set`에서 서브차트 이름을 키 접두사로 사용합니다 (예: `mysql.auth.rootPassword`).

---

## 명령어 가이드

### infra.sh 사용법

```bash
./k8s/infra.sh up        # 인프라 시작 (helm 설치)
./k8s/infra.sh down      # 인프라 중지 (데이터 유지)
./k8s/infra.sh clean     # 인프라 중지 + 데이터 삭제 (PVC 포함)
./k8s/infra.sh status    # 인프라 상태 확인
./k8s/infra.sh restart   # 인프라 재시작
```

### kubectl 기본 명령어

```bash
# 모든 리소스 상태 확인
kubectl get all,pvc -n modeunsa

# Pod 로그 확인
kubectl logs -f <pod-name> -n modeunsa

# Pod 쉘 접속
kubectl exec -it <pod-name> -n modeunsa -- /bin/bash

# Pod 상세 정보
kubectl describe pod <pod-name> -n modeunsa

# 이벤트 확인 (오류 디버깅)
kubectl get events -n modeunsa --sort-by='.lastTimestamp'
```

### helm 기본 명령어

```bash
# Chart 설치 (서브차트 구조 — values.yaml 자동 적용)
helm upgrade --install modeunsa-infra ./k8s/infra -n modeunsa

# .env 비밀값 주입하며 설치
helm upgrade --install modeunsa-infra ./k8s/infra -n modeunsa \
  --set mysql.auth.rootPassword="mypassword" \
  --set mysql.auth.database="mydb"

# 설치된 릴리즈 목록
helm list -n modeunsa

# 릴리즈 삭제
helm uninstall modeunsa-infra -n modeunsa

# 템플릿 렌더링 미리보기 (실제 배포 X)
helm template modeunsa-infra ./k8s/infra
```

---

## Helm 템플릿 문법

### 기본 문법 (서브차트)

서브차트 내부에서는 `.Values`가 해당 서브차트의 값을 직접 가리킵니다.

```yaml
# 값 참조 (서브차트에서는 서비스 이름 접두사 없이)
{{ .Values.image.tag }}

# 조건문
{{- if .Values.enabled }}
# enabled일 때만 이 리소스 생성
{{- end }}

# 헬퍼 함수 호출 (서브차트별 고유 이름)
{{ include "mysql.fullname" . }}

# 들여쓰기 (nindent = newline + indent)
labels:
  {{- include "mysql.labels" . | nindent 4 }}

# Base64 인코딩
{{ .Values.auth.rootPassword | b64enc | quote }}
```

### 헬퍼 함수 (_helpers.tpl)

각 서브차트는 자체 `_helpers.tpl`을 가집니다. `.Chart.Name`이 서브차트 이름(mysql, redis 등)이 됩니다.

```yaml
{{/* 리소스 이름 생성 — .Chart.Name이 서브차트 이름 */}}
{{- define "mysql.fullname" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "mysql.labels" -}}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}
```

### Secret 예시 (서브차트)

```yaml
{{- if .Values.enabled }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "mysql.fullname" . }}-secret
  labels:
    {{- include "mysql.labels" . | nindent 4 }}
type: Opaque
data:
  MYSQL_ROOT_PASSWORD: {{ .Values.auth.rootPassword | b64enc | quote }}
  MYSQL_DATABASE: {{ .Values.auth.database | b64enc | quote }}
{{- end }}
```

### StatefulSet 예시 (서브차트)

```yaml
{{- if .Values.enabled }}
apiVersion: apps/v1
kind: StatefulSet
metadata:
  name: {{ include "mysql.fullname" . }}
spec:
  serviceName: {{ include "mysql.fullname" . }}
  replicas: 1
  selector:
    matchLabels:
      app: {{ include "mysql.fullname" . }}
  template:
    spec:
      containers:
        - name: mysql
          image: "mysql:{{ .Values.image.tag }}"
          envFrom:
            - secretRef:
                name: {{ include "mysql.fullname" . }}-secret
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
{{- end }}
```

---

## Kubernetes 핵심 개념

### Pod vs Deployment vs StatefulSet

| 개념 | 설명 | 사용 사례 |
|-----|------|---------|
| **Pod** | 컨테이너의 최소 실행 단위 | 직접 사용 X |
| **Deployment** | 상태가 없는(Stateless) 앱 배포 | 웹 서버, API 서버 |
| **StatefulSet** | 상태가 있는(Stateful) 앱 배포 | DB, 캐시 |

### Service 타입

| 타입 | 설명 | 접속 방법 |
|-----|------|---------|
| **ClusterIP** | 클러스터 내부에서만 접근 | Traefik IngressRoute 또는 port-forward |
| **NodePort** | 노드 IP:Port로 외부 접근 | `localhost:30090` |
| **LoadBalancer** | 외부 로드밸런서 할당 | 클라우드 환경 |

### PVC (Persistent Volume Claim)

데이터를 영구 저장하기 위한 스토리지 요청:

```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mysql-pvc
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 5Gi
```

---

## 트러블슈팅

### Pod가 시작되지 않을 때

```bash
# Pod 상태 확인
kubectl get pods -n modeunsa

# 상세 정보 확인
kubectl describe pod <pod-name> -n modeunsa

# 이벤트 확인
kubectl get events -n modeunsa --sort-by='.lastTimestamp'
```

### 일반적인 에러와 해결

| 상태 | 원인 | 해결 방법 |
|-----|------|---------|
| `ImagePullBackOff` | 이미지를 가져올 수 없음 | 이미지 이름/태그 확인 |
| `CrashLoopBackOff` | 컨테이너가 반복 크래시 | `kubectl logs` 로 확인 |
| `Pending` | 리소스 부족 또는 PVC 바인딩 실패 | `describe`로 이유 확인 |
| `OOMKilled` | 메모리 부족 | `resources.limits.memory` 증가 |

### 서비스에 접속이 안 될 때

```bash
# Traefik Pod 상태 확인
kubectl get pod -l app=modeunsa-infra-traefik -n modeunsa

# IngressRouteTCP 확인
kubectl get ingressroutetcp -n modeunsa

# 재시작
./k8s/infra.sh restart
```

### 데이터 초기화가 필요할 때

```bash
# PVC 포함 전체 삭제
./k8s/infra.sh clean

# 다시 시작
./k8s/infra.sh up
```

---

## 환경 변수 (.env)

인프라 실행에 필요한 환경 변수를 `.env` 파일에 설정합니다:

```bash
# MySQL
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=modeunsa

# Redis
REDIS_PASSWORD=your_redis_password

# Grafana
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=your_grafana_password
```

> `.env` 파일은 프로젝트 루트에 위치해야 합니다.
