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
* **CORS**: 프론트엔드(`modeunsa.store`)와 로컬 환경(`localhost:3000`)에서의 API 요청을 허용하도록 설정되었습니다.

### 🛡 보안 그룹 (Inbound Rules)

| 포트 범위 | 프로토콜 | 원본 | 설명 |
| --- | --- | --- | --- |
| **443** | TCP | 0.0.0.0/0 | **HTTPS** (Nginx 접속 및 SSL) |
| **80** | TCP | 0.0.0.0/0 | **HTTP** (접속 시 443 리다이렉트용) |
| **22** | TCP | 0.0.0.0/0 | **SSH** (서버 원격 제어) |

---

## 3. 데이터베이스 및 캐시

### 💾 Self-managed MySQL

* 매니지드 서비스(RDS) 대신 EC2 내부에 직접 MySQL을 구축하여 운영합니다.
* `Member`, `OAuthAccount` 등 서비스의 모든 핵심 테이블 데이터가 이곳에 저장됩니다.
* **참고**: 서버 리소스 최적화를 위해 DB 엔진의 메모리 할당 및 주기적인 백업 설정이 필요합니다.

### ⚡ Redis (ElastiCache/Self-hosted)

* **Refresh Token**: 세션 유지 및 토큰 재발급을 위해 활용합니다.
* **Blacklist**: 로그아웃된 토큰을 만료 시점까지 저장하여 보안을 강화합니다.

---

## 4. S3 스토리지 및 객체 권한 정책

이미지 리소스의 효율적인 관리와 보안을 위해 **경로 기반의 Public Read 정책**을 적용했습니다.

### 📁 버킷 구조 및 접근 제어

* **버킷명**: `team01-storage`
* **권한**: 특정 서비스 디렉토리(`product`, `content`, `member`) 내의 객체에 대해서만 익명의 사용자가 읽기(`s3:GetObject`)를 할당했습니다.

### 📜 S3 Bucket Policy

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadImages",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": [
                "arn:aws:s3:::team01-storage/*/product/*",
                "arn:aws:s3:::team01-storage/*/content/*",
                "arn:aws:s3:::team01-storage/*/member/*"
            ]
        }
    ]
}

```