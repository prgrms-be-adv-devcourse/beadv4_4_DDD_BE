# 🌐 Nginx Configuration Guide

이 문서는 '모든사(modeunsa)' 프로젝트의 웹 서버 및 리버스 프록시 설정을 담당하는 Nginx 구성에 대해 설명합니다.

## 1. 개요 및 역할

Nginx는 EC2 내부의 최전방에서 클라이언트의 요청을 받아 처리하며, 다음과 같은 핵심 역할을 수행합니다.

* **HTTPS 종단점 (SSL Termination)**: Let's Encrypt 인증서를 통해 HTTPS 암호화 통신을 처리합니다.
* **리버프 프록시 (Reverse Proxy)**: 외부 요청을 내부 도커 컨테이너 서비스(`app:8080`)로 전달합니다.
* **포트 포워딩**: 80/443 포트 요청을 처리하여 사용자가 포트 번호 없이 접속할 수 있게 합니다.

---

## 2. 서버 및 프로토콜 설정

### 🔄 HTTP -> HTTPS 리다이렉션

사용자가 `http://modeunsa.store`로 접속할 경우, 보안을 위해 모든 요청을 HTTPS로 강제 전환(301 Redirect)합니다.

```nginx
server {
    listen 80;
    server_name modeunsa.store;
    return 301 https://$host$request_uri;
}

```

### 🔒 SSL/TLS 설정

Certbot을 통해 발급받은 Let's Encrypt 인증서를 적용했습니다.

* **인증서 경로**: `/etc/letsencrypt/live/modeunsa.store/`
* **보안 프로토콜**: `TLSv1.2`, `TLSv1.3`을 사용하여 강력한 암호화 수준을 유지합니다.

---

## 3. 프록시 및 로드 밸런싱

### 🚀 업스트림(Upstream) 설정

내부에서 실행 중인 스프링 부트 애플리케이션 서비스를 `app`이라는 이름으로 그룹화하여 관리합니다.

```nginx
upstream app {
    server app:8080;
}

```

### 📍 경로별 요청 처리

1. **API 요청 (`/api/v1`)**: 백엔드 API 서버로 요청을 전달하며, WebSocket 지원을 위한 `Upgrade` 및 `Connection` 헤더 설정을 포함합니다.
2. **루트 요청 (`/`)**: 모든 일반 요청을 백엔드 서비스로 포워딩합니다.
3. **헬스 체크 (`/health`)**: 서버의 생존 여부를 확인하기 위한 엔드포인트로, 로그를 남기지 않고 즉시 200 OK를 반환합니다.

---

## 4. 성능 및 최적화 설정

S3 이미지 업로드와 대규모 트래픽 처리를 위해 다음과 같은 최적화 설정을 적용했습니다.

* **파일 업로드 제한**: `client_max_body_size 10M` 설정을 통해 S3 업로드 시 발생할 수 있는 대용량 파일 전송을 허용합니다.
* **버퍼 최적화**: 프록시 버퍼 사이즈를 확장(`256k`)하여 대규모 응답 데이터 처리 시의 병목 현상을 방지했습니다.
* **타임아웃 설정**: 백엔드와의 연결 및 읽기 타임아웃을 `60s`로 설정하여 안정적인 연결을 유지합니다.

---

## 5. 장애 대응 (Error Handling)

백엔드 서버가 일시적으로 중단되었을 때(502, 503, 504 에러), 사용자에게 시스템적인 에러 페이지 대신 가독성 좋은 안내 메시지를 반환하도록 커스텀 설정되었습니다.

> **503 Service Temporarily Unavailable**: "Please try again later." 메시지를 통해 사용자 경험을 개선했습니다.

---

## 🛠 유지보수 팁

* **인증서 갱신**: Certbot 자동 갱신 확인을 위해 `sudo certbot renew --dry-run` 명령어를 주기적으로 확인하세요.
* **로그 확인**: 접속 로그는 `/var/log/nginx/access.log`, 에러 로그는 `/var/log/nginx/error.log`에서 실시간으로 확인할 수 있습니다.