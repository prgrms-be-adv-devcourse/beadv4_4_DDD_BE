# checkstyle & spotless 를 활용한 코드 컨벤션 커밋 강제 적용

### 커밋 전 git hook 실행
```makefile
git-hooks:
	@echo "Enabling repo hooks (.githooks)..."
	@git config core.hooksPath .githooks
	@chmod +x .githooks/pre-commit
	@echo "Done. (commit-msg & pre-commit hook active)"
```

### pre-commit.yml

```bash
#!/usr/bin/env bash
set -euo pipefail

echo "[pre-commit] Running Spotless (auto-fix)..."

# 스테이징된 java 변경이 없으면 스킵
if ! git diff --cached --name-only --diff-filter=ACM | grep -E '\.java$' > /dev/null; then
  echo "[pre-commit] No staged .java files. Skipping."
  exit 0
fi

# 1) 자동 수정
./gradlew -q spotlessApply

# 2) 자동 수정으로 바뀐 파일을 다시 스테이징에 올림
git add -u

echo "[pre-commit] Running Checkstyle..."

# Gradle Checkstyle 실행
./gradlew -q checkstyleMain checkstyleTest

echo "[pre-commit] Checkstyle passed."
```

1. java 파일의 변경이 없으면 skip
2. spotless 를 활용하여 포맷 자동화 실행 (spotlessApply)
3. 바뀐 파일 다시 스테이징에 올리기
4. gradle checkstyle 실행하여 규칙 위반 차단

### 플러그인 추가
```groovy
plugins {
    id 'checkstyle'
    id "com.diffplug.spotless" version "8.1.0" // lts
}
```

### spotless 정의
```groovy
spotless {
    java {
        googleJavaFormat("1.33.0")
        // diff 안정화를 위해 하기 옵션 추가
        trimTrailingWhitespace() // 줄 끝 공백 제거
        endWithNewline() // 파일 끝에 newline 강제
    }
}
```

### checkstyle 정의
```groovy
checkstyle {
    toolVersion = '13.0.0'
    maxWarnings = 0 // 경고가 0개 이상이라면 빌드 시에 에러 발생
    configFile = file("config/checkstyle/google_checks.xml") // rule 적용
    ignoreFailures = false // 위반 시 실패(강제)
}
```

### check task 생성 및 의존성 추가
```groovy
tasks.named('check') {
    dependsOn 'spotlessCheck', 'checkstyleMain', 'checkstyleTest'
}
```

### checkstyle 리포트 설정
```groovy
tasks.withType(Checkstyle).configureEach {
    reports {
        xml.required = false
        html.required = true
    }
}
```

### checkstyle 적용 소스 범위 제한
```groovy
// java 아래 폴더에만 적용, Build 나 QClass 에 영향 X
checkstyleMain.source = fileTree('src/main/java')
```