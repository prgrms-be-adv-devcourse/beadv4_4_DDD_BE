# 🏗 Infrastructure & AWS Cloud Setup

이 문서는 '모든사(modeunsa)' 백엔드 시스템의 AWS 인프라 구성, 그리고 S3 정적 리소스 관리 정책을 기록합니다.

## 1. 전체 아키텍처

* **Compute**: EC2 (Linux/UNIX) + **Elastic IP (탄력적 IP)**
* **Web Server**: **Nginx** (Reverse Proxy & SSL Termination)
* **Database**: **Self-hosted MySQL** (EC2 내부 설치) - 모든 도메인 데이터 저장
* **Storage**: **AWS S3** (`team01-storage`) - 상품, 회원, 콘텐츠 이미지 저장
* **DNS**: **Route 53** (`modeunsa.store`)

---

## 2. 도메인 및 네트워크 설정

### 🌐 접속 환경

* **고정 IP**: 탄력적 IP를 할당하여 서버 재시작 시에도 접속 주소가 변하지 않도록 설정했습니다.
* **HTTPS (Certbot)**: Nginx에 Let's Encrypt 인증서를 설치하여 포트 번호 없이 `https://modeunsa.store`로 보안 접속이 가능합니다.
* **CORS**: 프론트엔드(`modeunsa.store`)와 로컬 환경(`localhost:3000`, `localhost:8080`)에서의 API 요청을 허용하도록 설정되었습니다.

### 🛡 보안 그룹 (Inbound Rules)

| 포트 범위 | 프로토콜 | 원본 | 설명 |
| --- | --- | --- | --- |
| **443** | TCP | 0.0.0.0/0 | **HTTPS** (Nginx 접속 및 SSL) |
| **80** | TCP | 0.0.0.0/0 | **HTTP** (접속 시 443 리다이렉트용) |
| **22** | TCP | 0.0.0.0/0 | **SSH** (서버 원격 제어) |

---

## 3. S3 스토리지

### 📁 버킷 구조 및 접근 제어

* **버킷명**: `team01-storage`
* **권한 범위**:

    * `s3:PutObject`
    * `s3:GetObject`
    * `s3:DeleteObject`
    * `s3:ListBucket`
* **적용 대상**:

    * `arn:aws:s3:::team01-storage`
    * `arn:aws:s3:::team01-storage/*`

즉, 현재 정책은 특정 디렉토리 제한 없이 버킷 전체 객체에 대해 업로드/조회/삭제 및 목록 조회가 가능합니다.

### 📌 S3 CORS 정책

브라우저 기반 업로드 및 조회를 위해 CORS를 설정했습니다.

**허용 Origin**

* `http://localhost:3000`
* `https://modeunsa.store`

**허용 Method**

* `PUT`
* `GET`
* `HEAD`

**기타 설정**

* 모든 Header 허용 (`"*"`).
* `ETag` 헤더 노출.
* Preflight 캐시 유지 시간: 3000초.