# Kubernetes 인프라 가이드

## 목차

1. [사전 준비](#사전-준비)
2. [빠른 시작](#빠른-시작)
3. [Prod 환경 배포 (EC2 + k3s)](#prod-환경-배포-ec2--k3s)
4. [리소스 타입 요약](#리소스-타입-요약)
5. [환경 변수](#환경-변수-env)
6. [kubectl 디버깅 명령어](#kubectl-디버깅-명령어)
7. [트러블슈팅](#트러블슈팅)

---

## 사전 준비

### macOS

```bash
# 1. Colima 및 Docker CLI 설치
brew install colima docker

# 2. kubectl, helm 설치
brew install kubectl helm
```

> Colima(k3s)는 `infra.sh up` 실행 시 자동 시작됩니다. / Docker Desktop은 불필요합니다.
> Apple Silicon(M시리즈) Mac에서 Rosetta를 통해 x86 컨테이너를 실행합니다 (`--vz-rosetta`).

#### Colima VM 메모리 설정

API pod를 포함한 전체 pod를 실행하려면 VM에 충분한 메모리가 필요합니다.

| 서비스 | Memory Limit |
|--------|-------------|
| API | 1536Mi |
| Elasticsearch | 1280Mi |
| MySQL | 768Mi |
| Kafka | 512Mi |
| Kafka UI | 384Mi |
| Grafana | 256Mi |
| Prometheus | 128Mi |
| Redis | 128Mi |
| **Pod 합계** | **~5Gi** |
| k3s 시스템 오버헤드 | ~1Gi |
| **전체 필요량** | **~6Gi** |

```bash
# VM 메모리 변경 (기존 VM이 있는 경우)
colima stop
colima start --memory 7 --cpu 2 --kubernetes
```

> VM 메모리가 모든 pod의 limits 합 + 시스템 오버헤드보다 작으면 부하 시 OOMKilled가 발생할 수 있습니다.

### Windows

Colima는 macOS/Linux 전용이므로 Windows에서는 Rancher Desktop을 사용합니다 (동일한 k3s 기반).

1. **Rancher Desktop 설치**: https://rancherdesktop.io/
   - Container Engine: `dockerd (moby)` 선택
   - Kubernetes 활성화 확인

2. **kubectl, helm 설치**
   ```powershell
   winget install Kubernetes.kubectl
   winget install Helm.Helm
   ```

3. **Git Bash에서 스크립트 실행**
   - IntelliJ > Settings > Tools > Terminal > Shell path를 `C:\Program Files\Git\bin\bash.exe`로 변경

---

## 빠른 시작

### 1. 환경 파일 설정

```bash
# 템플릿 복사
cp .env.example .env.k3s.dev     # 개발 환경
cp .env.example .env.k3s-prod    # 운영 환경

# K3s용 호스트 설정으로 변경
vi .env.k3s.dev
```

### 2. 실행

| 스크립트 | 역할 |
|---------|------|
| `./k8s/infra.sh` | 인프라만 관리 (MySQL, Redis, ES 등) |
| `./k8s/app.sh` | 애플리케이션만 관리 (API 서버) |
| `./k8s/deploy.sh` | 인프라 + 앱 통합 관리 |

```bash
# 인프라만
./k8s/infra.sh up dev       # 인프라 시작 (dev 환경)
./k8s/infra.sh up prod      # 인프라 시작 (prod 환경)
./k8s/infra.sh down         # 인프라 중지 (데이터 유지)
./k8s/infra.sh clean        # 인프라 삭제 (데이터 포함)
./k8s/infra.sh status       # 상태 확인

# 앱만 (인프라 실행 중이어야 함)
./k8s/app.sh up dev         # 앱 배포 (dev 환경)
./k8s/app.sh up prod        # 앱 배포 (prod 환경)
./k8s/app.sh down           # 앱 중지
./k8s/app.sh logs           # 앱 로그

# 전체 (인프라 + 앱)
./k8s/deploy.sh up          # 전체 시작
./k8s/deploy.sh down        # 전체 중지
./k8s/deploy.sh clean       # 전체 삭제
```

### 접속 정보

| 서비스        | 접속 주소              | 접속 방식  |
|--------------|----------------------|-----------|
| API Server   | `localhost:30080`    | NodePort  |
| MySQL        | `localhost:30306`    | NodePort  |
| Redis        | `localhost:30379`    | NodePort  |
| Elasticsearch| `localhost:30920`    | NodePort  |
| Kafka        | `localhost:30092`    | NodePort  |
| Kafka UI     | `localhost:30085`    | NodePort  |
| Prometheus   | `localhost:30090`    | NodePort  |
| Grafana      | `localhost:30300`    | NodePort  |

---

## Prod 환경 배포 (EC2 + k3s)

### 1. EC2에 k3s 설치

```bash
curl -sfL https://get.k3s.io | sh -
sudo chmod 644 /etc/rancher/k3s/k3s.yaml
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
```

### 2. cert-manager 설치 (HTTPS 인증서 자동 발급)

```bash
kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.14.4/cert-manager.yaml
kubectl wait --for=condition=Available deployment --all -n cert-manager --timeout=120s
```

### 3. 배포

```bash
./k8s/infra.sh up prod    # MySQL, Redis, Grafana, Prometheus + ClusterIssuer
./k8s/app.sh up prod      # API, Frontend + Ingress (TLS)
```

### Prod 접속 정보

| 서비스 | 접속 주소 |
|--------|----------|
| Frontend | `https://modeunsa.store/` |
| API | `https://modeunsa.store/api` |
| Grafana | `https://modeunsa.store/grafana` |
| Prometheus | `https://modeunsa.store/prometheus` |

> Traefik(k3s 내장)이 Ingress Controller로 동작하며, cert-manager가 Let's Encrypt 인증서를 자동 발급/갱신합니다.

---

## 리소스 타입 요약

| 리소스 | 역할 |
|--------|------|
| **StatefulSet** | Pod 이름·스토리지 고정. 재시작해도 동일 PVC에 연결 (DB, 캐시용) |
| **Deployment** | Pod 이름 랜덤. 롤링 업데이트·스케일링 용이 (API, 모니터링용) |
| **PVC** | Pod 삭제 후에도 데이터 유지. `infra.sh clean` 시에만 삭제 |
| **Secret** | 비밀번호·API 키 저장. `stringData`로 평문 작성 → K8s가 자동 base64 인코딩 |
| **ConfigMap** | 설정 파일을 K8s 리소스로 관리 (prometheus.yml) |
| **NodePort** | `localhost:30xxx`로 외부 접근. 로컬 개발용 (프로덕션에서는 ClusterIP + Ingress) |
| **initContainer** | 메인 컨테이너 시작 전 실행. ES 플러그인 설치처럼 일회성 초기화에 사용 |

---

## 환경 변수 설정

### 환경 파일 생성

```bash
# 템플릿에서 환경 파일 생성
cp .env.example .env.k3s.dev     # 개발 환경
cp .env.example .env.k3s-prod    # 운영 환경

# 환경 파일 편집 (호스트 설정 변경 필요)
vi .env.k3s.dev
```

### K3s용 호스트 설정

K3s 환경에서는 서비스 이름을 사용해야 합니다. `.env.k3s.*` 파일에서 다음과 같이 설정하세요:

```bash
# Database
DB_HOST=modeunsa-infra-mysql
DB_PORT=3306

# Redis
REDIS_HOST=modeunsa-infra-redis
REDIS_PORT=6379

# Elasticsearch
ES_HOST=modeunsa-infra-elasticsearch
ES_PORT=9200
SPRING_ELASTICSEARCH_URIS=http://modeunsa-infra-elasticsearch:9200
```

### 필수 환경 변수

```bash
# Database
MYSQL_ROOT_PASSWORD=your_password
MYSQL_DATABASE=modeunsa

# Redis
REDIS_PASSWORD=your_redis_password

# Grafana
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=your_grafana_password

# Docker Image (app 배포 시 필요)
DOCKER_IMAGE=your-registry/modeunsa:latest
```

---

## kubectl 디버깅 명령어

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

---

## 트러블슈팅

### 일반적인 에러와 해결

| 상태 | 원인 | 해결 방법 |
|-----|------|---------|
| `ImagePullBackOff` | 이미지를 가져올 수 없음 | 이미지 이름/태그 확인 |
| `CrashLoopBackOff` | 컨테이너가 반복 크래시 | `kubectl logs --previous` 로 확인, 아래 참조 |
| `Pending` | 리소스 부족 또는 PVC 바인딩 실패 | `describe`로 이유 확인 |
| `OOMKilled` | 메모리 부족 | `resources.limits.memory` 증가 |

### API Pod CrashLoopBackOff

API pod가 CrashLoopBackOff에 빠질 때 확인할 사항:

1. **Spring Profile 확인**: `values.yaml`의 `springProfile`이 `prod`인지 확인. `dev` 프로필은 localhost 주소를 사용하므로 K8s 클러스터 내부에서 동작하지 않음.
2. **메모리 부족(OOMKilled)**: `kubectl describe pod <name> -n modeunsa`에서 OOMKilled 확인. API pod의 memory limit이 2Gi 이상인지 확인.
3. **인프라 서비스 상태**: MySQL, Redis, Elasticsearch pod가 Running 상태인지 확인.
4. **Actuator 엔드포인트**: readiness/liveness probe가 `/actuator/health`를 체크하므로, Spring Security에서 해당 경로가 permit 되어 있어야 함.

```bash
# 크래시 로그 확인
kubectl logs <pod-name> -n modeunsa --previous

# Pod 상세 정보 (OOMKilled 등 확인)
kubectl describe pod <pod-name> -n modeunsa
```

### 서비스에 접속이 안 될 때

```bash
# 서비스 상태 확인
kubectl get svc -n modeunsa

# NodePort가 정상적으로 할당되었는지 확인
kubectl describe svc -n modeunsa

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
