# 🛠 CI (Continuous Integration) 가이드

이 문서는 **modeunsa** 프로젝트의 코드 품질을 유지하고, 기능 결함을 사전에 방지하기 위한 지속적 통합(CI) 절차를 설명합니다. 모든 개발자는 브랜치에 코드를 머지하기 전, 이 가이드에 정의된 자동화 검증을 통과해야 합니다.

## 1. CI 파이프라인 개요

우리 프로젝트는 **GitHub Actions**를 활용하여 `main` 및 `develop` 브랜치로 향하는 모든 Pull Request를 자동으로 검증합니다.

* **목적**: 코드 스타일 통일, 컴파일 오류 확인, 로직 안정성 검증
* **트리거**: `main`, `develop` 브랜치에 대한 Pull Request 생성, 업데이트, 재오픈 시

---

## 2. 주요 검증 단계 (Jobs)

### ✅ 코드 스타일 점검 (`code-style`)

팀원 간 일관된 코드 포맷을 유지하기 위해 정적 분석 도구를 실행합니다.

* **Spotless Check**: Google Java Style Guide 등을 기반으로 자동 포맷팅 준수 여부를 확인합니다.
* **Checkstyle**: 변수명, 들여쓰기, 메서드 길이 등 상세 코딩 컨벤션을 점검합니다.
* **결과 확인**: 실패 시 GitHub Actions 탭에서 `checkstyle-report` 아티팩트를 다운로드하여 위반 사항을 확인할 수 있습니다.

### 🛠 빌드 가능성 확인 (`build`)

애플리케이션이 정상적으로 컴파일되는지 빠르게 확인합니다.

* **최적화**: 테스트와 스타일 체크를 제외하여 빌드 속도를 극대화했습니다.
* **환경**: **Java 21 (Temurin)** 및 **Gradle** 환경에서 수행됩니다.

### 🧪 자동화 통합 테스트 (`test`)

비즈니스 로직의 결함을 찾아내기 위해 실제 구동 환경과 유사한 통합 테스트를 수행합니다.

* **서비스 컨테이너**: 테스트 중 Redis 연동 로직을 검증하기 위해 **Redis 7-alpine** 이미지를 서비스 컨테이너로 직접 띄워 활용합니다.
* **보안 환경**: 데이터 암호화 로직 검증을 위해 `ENCRYPTION_MASTER_KEY`를 GitHub Secrets로부터 주입받습니다.
* **테스트 리포트**: 성공/실패 여부와 관계없이 항상 상세 `test-report`를 업로드하여 실패 지점을 추적할 수 있도록 합니다.

---

## 3. 보안 및 환경 설정

CI 과정에서 사용하는 민감한 정보는 GitHub 리포지토리의 **Secrets**에서 관리합니다.

* **`ENCRYPTION_MASTER_KEY`**: 테스트 코드 내에서 암호화/복호화 로직이 동작하기 위해 반드시 필요한 키입니다.
* **Gradle Cache**: 빌드 속도 향상을 위해 Gradle 의존성 패키지를 캐싱하여 사용합니다.

---

## 4. CI 실패 시 대응 가이드

CI 파이프라인이 빨간색(Failure)으로 표시되면 머지가 불가능합니다. 아래 순서대로 대응하세요.

1. **Style 실패**: 로컬 환경에서 `./gradlew -q spotlessApply`, `./gradlew -q checkstyleMain checkstyleTest` 명령어를 실행하여 포맷을 수정한 뒤 다시 Push 하세요.
2. **Test 실패**: GitHub Actions의 아티팩트에서 `test-report`를 다운로드하여 실패한 테스트 케이스와 에러 로그를 분석하세요.
3. **Build 실패**: 로컬 환경에서 `./gradlew clean build -x test`를 실행하여 컴파일 에러가 없는지 확인하세요.