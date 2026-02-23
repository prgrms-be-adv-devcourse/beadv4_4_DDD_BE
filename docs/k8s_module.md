# 새 백엔드 모듈을 k3s에 배포하는 방법

`settlement-api`가 헬름 차트로 k3s에 배포되는 방식을 기준으로, 새 백엔드 모듈을 동일하게 배포하기 위한 단계별 가이드입니다.

---

## 개요: 수정/추가해야 하는 파일 목록

| 파일 | 설명 |
|------|------|
| `<module>-service/` | Gradle 모듈 디렉토리 이름 규칙 |
| `k8s/app/charts/<module-name>/Chart.yaml` | 서브차트 메타데이터 |
| `k8s/app/charts/<module-name>/values.yaml` | 기본값 (포트, 리소스 등) |
| `k8s/app/charts/<module-name>/templates/_helpers.tpl` | 템플릿 헬퍼 함수 |
| `k8s/app/charts/<module-name>/templates/deployment.yaml` | Deployment 리소스 |
| `k8s/app/charts/<module-name>/templates/service.yaml` | Service 리소스 |
| `k8s/app/charts/<module-name>/templates/secret.yaml` | Secret 리소스 |
| `k8s/app/Chart.yaml` | 부모 차트 dependencies 등록 |
| `k8s/app/values.yaml` | 부모 차트 enabled 항목 등록 |
| `k8s/app/values-prod.yaml` | prod 환경 오버라이드 (ClusterIP, springProfile 등) |
| `k8s/app.sh` | `BACKEND_MODULES` 배열에 모듈 등록 |

---

## Step 1: 모듈 디렉토리 이름 규칙

모듈 디렉토리는 반드시 `*-service` 형식으로 이름을 지정해야 합니다.

```
settlement-service/   # ✅ OK
content-service/      # ✅ OK
settlement/           # ❌ 이미지 빌드 대상에서 제외됨
```

`make backend dev/prod`는 루트에서 `*-service` 패턴의 디렉토리를 자동으로 감지합니다.

---

## Step 2: `application.yml` / `application-k3s-*.yml` 정합성 점검 (중요)

모듈별 기본 설정이 `settlement`와 다르면, 이미지 빌드/배포는 성공해도 k3s 내부 연결이 실패할 수 있습니다.

체크 포인트:

1. `application-k3s-dev.yml`에서 infra 서비스명을 사용
   - DB: `modeunsa-infra-mysql:3306`
   - Redis: `modeunsa-infra-redis:6379`
   - Kafka: `modeunsa-infra-kafka:9092`
   - Elasticsearch: `modeunsa-infra-elasticsearch:9200`
2. `${REDIS_HOST}`처럼 환경변수로 쓰이는 부분은 로컬 환경에서 IntelliJ로 띄운 모듈을 위한 것이고, 실제 k3s 내부에서 
pod들은 `modeunsa-infra-mysql`와 같은 값을 사용합니다. 변하지 않을 값들은 헬름 차트의 `values.yaml`에서 직접적으로
선언해놓습니다.

예시(권장):

```yaml
# application-k3s-dev.yml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:modeunsa-infra-mysql}:${DB_PORT:3306}/${DB_NAME:modeunsa}
  data:
    redis:
      host: ${REDIS_HOST:modeunsa-infra-redis}
      port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:modeunsa-infra-kafka:9092}
  elasticsearch:
    uris:
      - http://${ES_HOST:modeunsa-infra-elasticsearch}:${ES_PORT:9200}
```

---

## Step 3: 이미지 빌드 & 푸시

```bash
make backend prod
```

대화형으로 진행됩니다:

1. 감지된 `*-service` 모듈 목록이 출력됨
2. 번호를 선택하면 해당 모듈을 빌드
3. Docker Hub 이미지 경로와 새 버전을 입력
4. `linux/amd64,linux/arm64` 멀티 플랫폼 빌드 후 자동 push

**IMAGE_KEY 자동 계산 규칙:**

```
<module>-service → <MODULE>_IMAGE
```

예:
- `settlement-service` → `SETTLEMENT_IMAGE`
- `content-service` → `CONTENT_IMAGE`
- `user-service` → `USER_IMAGE`

**완료 후 `.env.k3s-prod`가 자동으로 업데이트됩니다:**

```bash
# .env.k3s-prod (자동 업데이트된 예시)
SETTLEMENT_IMAGE=chanheess/modeunsa-settlement:0.0.2
CONTENT_IMAGE=chanheess/modeunsa-content:0.0.1
```

**`.env.k3s-prod`를 최신화 해주기 위해 Google Drive에 업로드하세요:**

---

## Step 4: 헬름 차트 작성

`settlement-api`를 기준으로 `k8s/app/charts/<module-name>/` 디렉토리를 만들고 6개 파일을 작성합니다.

> 아래 예시에서 `settlement-api`를 추가할 모듈 이름으로 치환하세요.

### 3-1. `Chart.yaml`

```yaml
apiVersion: v2
name: <module-name>          # 예: content-api
description: Modeunsa <Module> Server
type: application
version: 0.1.0
```

### 3-2. `values.yaml`

```yaml
enabled: true
image:
  repository: ""
  tag: "latest"
  pullPolicy: Always
replicas: 1
service:
  type: NodePort
  port: <PORT>          # 모듈별 고유 포트 (예: 8085)
  nodePort: <NODE_PORT> # 30000번대 고유 NodePort (예: 30085)
env:
  springProfile: "k3s-dev"
  dbHost: "modeunsa-infra-mysql"
  dbPort: "3306"
  dbName: ""
  redisHost: "modeunsa-infra-redis"
  redisPort: "6379"
  kafkaBootstrapServers: "modeunsa-infra-kafka:9092"
  esHost: "modeunsa-infra-elasticsearch"
  esPort: "9200"
secrets:
  dbUsername: "root"
  dbPassword: ""
  redisPassword: ""
  jwtSecret: ""
  kakaoClientId: ""
  kakaoClientSecret: ""
  naverClientId: ""
  naverClientSecret: ""
  awsAccessKey: ""
  awsSecretKey: ""
  encryptionMasterKey: ""
  tossPaymentsSecretKey: ""
  internalApiKey: ""
javaOpts: "-Xmx512m"
resources:
  requests:
    cpu: 150m
    memory: 512Mi
  limits:
    cpu: 400m
    memory: 1536Mi
```

### 3-3. `templates/_helpers.tpl`

```
{{/* 리소스 이름 */}}
{{- define "<camelCaseModuleName>.fullname" -}}
{{- printf "%s-%s" .Release.Name "<module-name>" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/* 공통 라벨 */}}
{{- define "<camelCaseModuleName>.labels" -}}
app.kubernetes.io/name: <module-name>
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/* 셀렉터 라벨 */}}
{{- define "<camelCaseModuleName>.selectorLabels" -}}
app: {{ include "<camelCaseModuleName>.fullname" . }}
{{- end }}
```

예: `settlement-api` → define 이름은 `settlementApi`

### 3-4. `templates/deployment.yaml`

```yaml
{{- if .Values.enabled }}
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "<camelCaseModuleName>.fullname" . }}
  labels:
    {{- include "<camelCaseModuleName>.labels" . | nindent 4 }}
spec:
  replicas: {{ .Values.replicas }}
  selector:
    matchLabels:
      {{- include "<camelCaseModuleName>.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      labels:
        {{- include "<camelCaseModuleName>.selectorLabels" . | nindent 8 }}
    spec:
      containers:
        - name: <module-name>
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - containerPort: {{ .Values.service.port }}
              protocol: TCP
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: {{ .Values.env.springProfile | quote }}
            - name: DB_HOST
              value: {{ .Values.env.dbHost | quote }}
            - name: DB_PORT
              value: {{ .Values.env.dbPort | quote }}
            - name: DB_NAME
              value: {{ .Values.env.dbName | quote }}
            - name: MYSQL_DATABASE
              value: {{ .Values.env.dbName | quote }}
            - name: REDIS_HOST
              value: {{ .Values.env.redisHost | quote }}
            - name: REDIS_PORT
              value: {{ .Values.env.redisPort | quote }}
            - name: KAFKA_BOOTSTRAP_SERVERS
              value: {{ .Values.env.kafkaBootstrapServers | quote }}
            - name: ES_HOST
              value: {{ .Values.env.esHost | quote }}
            - name: ES_PORT
              value: {{ .Values.env.esPort | quote }}
            - name: SPRING_ELASTICSEARCH_URIS
              value: "http://{{ .Values.env.esHost }}:{{ .Values.env.esPort }}"
            {{- if .Values.javaOpts }}
            - name: JAVA_TOOL_OPTIONS
              value: {{ .Values.javaOpts | quote }}
            {{- end }}
          envFrom:
            - secretRef:
                name: {{ include "<camelCaseModuleName>.fullname" . }}-secret
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          startupProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.port }}
            initialDelaySeconds: 30
            periodSeconds: 10
            failureThreshold: 30
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.port }}
            periodSeconds: 10
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: {{ .Values.service.port }}
            periodSeconds: 30
{{- end }}
```

### 3-5. `templates/service.yaml`

```yaml
{{- if .Values.enabled }}
apiVersion: v1
kind: Service
metadata:
  name: {{ include "<camelCaseModuleName>.fullname" . }}
  labels:
    {{- include "<camelCaseModuleName>.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: {{ .Values.service.port }}
      {{- if eq .Values.service.type "NodePort" }}
      nodePort: {{ .Values.service.nodePort }}
      {{- end }}
      protocol: TCP
      name: http
  selector:
    {{- include "<camelCaseModuleName>.selectorLabels" . | nindent 4 }}
{{- end }}
```

### 3-6. `templates/secret.yaml`

```yaml
{{- if .Values.enabled }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "<camelCaseModuleName>.fullname" . }}-secret
  labels:
    {{- include "<camelCaseModuleName>.labels" . | nindent 4 }}
type: Opaque
stringData:
  DB_USERNAME: {{ .Values.secrets.dbUsername | quote }}
  DB_PASSWORD: {{ .Values.secrets.dbPassword | quote }}
  REDIS_PASSWORD: {{ .Values.secrets.redisPassword | quote }}
  JWT_SECRET: {{ .Values.secrets.jwtSecret | quote }}
  KAKAO_CLIENT_ID: {{ .Values.secrets.kakaoClientId | quote }}
  KAKAO_CLIENT_SECRET: {{ .Values.secrets.kakaoClientSecret | quote }}
  NAVER_CLIENT_ID: {{ .Values.secrets.naverClientId | quote }}
  NAVER_CLIENT_SECRET: {{ .Values.secrets.naverClientSecret | quote }}
  AWS_ACCESS_KEY: {{ .Values.secrets.awsAccessKey | quote }}
  AWS_SECRET_KEY: {{ .Values.secrets.awsSecretKey | quote }}
  ENCRYPTION_MASTER_KEY: {{ .Values.secrets.encryptionMasterKey | quote }}
  TOSS_PAYMENTS_SECRET_KEY: {{ .Values.secrets.tossPaymentsSecretKey | quote }}
  INTERNAL_API_KEY: {{ .Values.secrets.internalApiKey | quote }}
{{- end }}
```

---

## Step 5: 부모 차트에 등록

### `k8s/app/Chart.yaml`

`dependencies`에 새 서브차트를 추가합니다:

```yaml
dependencies:
  - name: api
    version: "0.1.0"
    condition: api.enabled
  - name: settlement-api
    version: "0.1.0"
    condition: settlement-api.enabled
  - name: <module-name>        # 추가
    version: "0.1.0"
    condition: <module-name>.enabled
  - name: frontend
    version: 0.1.0
    condition: frontend.enabled
```

### `k8s/app/values.yaml`

새 모듈의 enabled 항목을 추가합니다:

```yaml
api:
  enabled: false

settlement-api:
  enabled: false

<module-name>:      # 추가
  enabled: false

frontend:
  enabled: false
```

### `k8s/app/values-prod.yaml`

prod 환경에서는 `values.yaml`의 기본값을 덮어쓰는 `values-prod.yaml`에도 등록해야 합니다.
서비스 타입이 `NodePort` → `ClusterIP`로 변경되고, `enabled: true`로 활성화됩니다.

```yaml
<module-name>:      # 추가
  enabled: true
  image:
    pullPolicy: IfNotPresent
  service:
    type: ClusterIP
  env:
    springProfile: *backendCommonProdSpringProfile
  resources:
    limits:
      cpu: 1
      memory: 1536Mi
```

---

## Step 6: `app.sh` BACKEND_MODULES 등록

`k8s/app.sh`의 `BACKEND_MODULES` 배열에 새 모듈을 추가합니다.

형식: `"<chart-name>|<IMAGE_ENV_KEY>|<표시명>|<dev접속주소>|<required|optional>"`

```bash
BACKEND_MODULES=(
  "api|DOCKER_IMAGE|API Server|localhost:30080|required"
  "settlement-api|SETTLEMENT_IMAGE|Settlement|localhost:30084|optional"
  "<module-name>|<MODULE>_IMAGE|<표시명>|localhost:<nodePort>|optional"  # 추가
)
```

예: content 모듈 추가 시:
```bash
"content-api|CONTENT_IMAGE|Content|localhost:30085|optional"
```

`IMAGE_ENV_KEY`는 Step 3에서 자동 계산된 키와 일치해야 합니다 (`CONTENT_IMAGE` 등).

---

## Step 7: 배포

### 로컬 → EC2 파일 전송

```bash
# EC2에 k8s 디렉토리 및 .env.k3s-prod 전송
scp -i <pem-path> -r k8s/ <user>@<EC2-IP>:<app-dir>
scp -i <pem-path> .env.k3s-prod <user>@<EC2-IP>:<app-dir>/.env.k3s-prod
```

예:

```bash
scp -i /Users/chpark/Documents/aws/modeunsa_pem.pem -r k8s/ ec2-user@52.79.155.221:/home/ec2-user/app
scp -i /Users/chpark/Documents/aws/modeunsa_pem.pem .env.k3s-prod ec2-user@52.79.155.221:/home/ec2-user/app/.env.k3s-prod
```

> `.env.k3s-prod`는 Step 3(`make backend prod`)에서 이미 업데이트되었습니다.

### EC2에서 배포 실행

```bash
ssh <user>@<EC2-IP>
cd ~/beadv4

./app/k8s/app.sh up prod
```

배포 완료 후 Pod 상태 확인:

```bash
./app/k8s/app.sh status
# 또는
kubectl get pods -n modeunsa
```

로그 확인:

```bash
./app/k8s/app.sh logs <module-name>
```

명령어가 안 될 경우 **sudo** 붙여서 해보기
