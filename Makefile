# =========================
# Env loading
# =========================
ifneq (,$(wildcard .env))
	include .env
	export
endif

.PHONY: backend frontend dev prod \
	release-backend release-backend-dev release-frontend release-frontend-dev \
	release-backend-core release-frontend-core

git-setup: git-template git-hooks
	@echo "Done. (repo-local git template + hooks applied)"

git-template:
	@echo "Setting git commit template..."
	@git config commit.template .gitmessage.txt
	@echo "Done."

git-hooks:
	@echo "Enabling repo hooks (.githooks)..."
	@git config core.hooksPath .githooks
	@chmod +x .githooks/commit-msg
	@chmod +x .githooks/pre-commit
	@echo "Done. (commit-msg & pre-commit hook active)"

# Code quality and formatting
format-check:
	@echo "Checking code formatting..."
	@./gradlew spotlessCheck

format-apply:
	@echo "Applying code formatting..."
	@./gradlew -q spotlessApply

build:
	@echo "Gradle clean & build"
	./gradlew clean build

docker-build:
	@echo "Building image: $(DOCKER_IMAGE)"
	docker buildx build --platform linux/amd64,linux/arm64 -t $(DOCKER_IMAGE) .

docker-push:
	@echo "Pushing image: $(DOCKER_IMAGE)"
	docker push $(DOCKER_IMAGE)

multi-docker-build-push:
	docker buildx build \
      --platform linux/amd64,linux/arm64 \
      -t $(DOCKER_IMAGE) \
      --push .

# =========================
# Convenience Targets
# Usage:
#   make backend dev
#   make backend prod
#   make frontend dev
#   make frontend prod
# =========================
backend:
	@ENV_TARGET=$$(echo "$(MAKECMDGOALS)" | tr ' ' '\n' | grep -E '^(dev|prod)$$' | head -1); \
	if [ -z "$$ENV_TARGET" ]; then ENV_TARGET=prod; fi; \
	if [ "$$ENV_TARGET" = "dev" ]; then \
		$(MAKE) --no-print-directory release-backend-dev; \
	else \
		$(MAKE) --no-print-directory release-backend; \
	fi

frontend:
	@ENV_TARGET=$$(echo "$(MAKECMDGOALS)" | tr ' ' '\n' | grep -E '^(dev|prod)$$' | head -1); \
	if [ -z "$$ENV_TARGET" ]; then ENV_TARGET=prod; fi; \
	if [ "$$ENV_TARGET" = "dev" ]; then \
		$(MAKE) --no-print-directory release-frontend-dev; \
	else \
		$(MAKE) --no-print-directory release-frontend; \
	fi

dev:
	@:

prod:
	@:

# =========================
# Backend: Build & Push
# =========================
release-backend:
	@$(MAKE) --no-print-directory release-backend-core ENV_FILE=.env.k3s-prod VERSION_MODE=input MODE_LABEL="Backend Release"

release-backend-dev:
	@$(MAKE) --no-print-directory release-backend-core ENV_FILE=.env.k3s-dev VERSION_MODE=latest MODE_LABEL="Backend Release (dev)"

release-backend-core:
	@set -e; \
	upsert_image_key() { \
		key="$$1"; value="$$2"; file="$$3"; \
		if grep -q "^$$key=" "$$file"; then \
			sed -i '' "s|^$$key=.*|$$key=$$value|" "$$file"; \
		else \
			if grep -q '^# Docker Image' "$$file"; then \
				awk -v k="$$key" -v v="$$value" '\
					BEGIN { inDocker=0; inserted=0 } \
					{ \
						if ($$0 ~ /^# Docker Image/) { inDocker=1; print; next } \
						if (inDocker && ($$0 ~ /^# / || $$0 ~ /^$$/)) { \
							if (!inserted) { print k "=" v; inserted=1 } \
							inDocker=0; \
						} \
						print; \
					} \
					END { if (!inserted) { print k "=" v } }' "$$file" > "$$file.tmp" && mv "$$file.tmp" "$$file"; \
			else \
				printf "# Docker Image\n%s=%s\n" "$$key" "$$value" >> "$$file"; \
			fi; \
		fi; \
	}; \
	SERVICE_MODULES=$$(find . -maxdepth 1 -type d -name '*-service' | sed 's|^\./||' | sort); \
	if [ -z "$$SERVICE_MODULES" ]; then echo "선택 가능한 *-service 모듈이 없습니다."; exit 1; fi; \
	echo ""; \
	echo "=== $(MODE_LABEL) ==="; \
	echo "릴리즈할 모듈을 선택하세요:"; \
	echo "$$SERVICE_MODULES" | nl -w1 -s'. '; \
	echo ""; \
	read -p "번호를 입력하세요: " CHOICE; \
	if ! echo "$$CHOICE" | grep -Eq '^[0-9]+$$'; then echo "올바른 번호를 입력해주세요."; exit 1; fi; \
	MODULE=$$(echo "$$SERVICE_MODULES" | sed -n "$${CHOICE}p"); \
	if [ -z "$$MODULE" ]; then echo "선택한 번호가 범위를 벗어났습니다."; exit 1; fi; \
	IMAGE_KEY=$$(echo "$$MODULE" | sed 's/-service$$//' | tr '[:lower:]-' '[:upper:]_')_IMAGE; \
	CURRENT_IMG=$$(grep "^$$IMAGE_KEY=" "$(ENV_FILE)" | head -1 | cut -d= -f2- || true); \
	if [ -n "$$CURRENT_IMG" ] && echo "$$CURRENT_IMG" | grep -q ':'; then REPO=$${CURRENT_IMG%:*}; else REPO="$$CURRENT_IMG"; fi; \
	if [ -z "$$REPO" ]; then \
		echo "$$IMAGE_KEY 값이 비어 있습니다."; \
		read -p "이미지 경로(태그 제외)를 입력하세요 (예: chanheess/modeunsa-settlement): " REPO; \
		if [ -z "$$REPO" ]; then echo "이미지 경로를 입력해주세요."; exit 1; fi; \
	fi; \
	if [ "$(VERSION_MODE)" = "latest" ]; then \
		VERSION=latest; \
		echo "dev는 latest 태그를 사용합니다: $$REPO:$$VERSION"; \
	else \
		if [ -n "$$CURRENT_IMG" ]; then echo "현재 버전: $$CURRENT_IMG"; else echo "현재 버전: (비어있음)"; fi; \
		echo ""; \
		read -p "새 버전을 입력하세요 (예: 0.0.1): " VERSION; \
		if [ -z "$$VERSION" ]; then echo "버전을 입력해주세요."; exit 1; fi; \
	fi; \
	echo ""; \
	echo "[1/2] $$MODULE 빌드..."; \
	./gradlew :$$MODULE:clean :$$MODULE:bootJar -x test && \
	echo "[2/2] Docker build & push: $$REPO:$$VERSION (context: $$MODULE/)"; \
	docker buildx build --platform linux/amd64,linux/arm64 -f Dockerfile -t $$REPO:$$VERSION --push $$MODULE/ && \
	upsert_image_key "$$IMAGE_KEY" "$$REPO:$$VERSION" "$(ENV_FILE)" && \
	echo "" && \
	echo "Backend release 완료: $$IMAGE_KEY=$$REPO:$$VERSION" && \
	echo "   $(ENV_FILE) 업데이트 완료"

# =========================
# Frontend: Build & Push
# =========================
release-frontend:
	@$(MAKE) --no-print-directory release-frontend-core ENV_FILE=.env.k3s-prod VERSION_MODE=input MODE_LABEL="Frontend Release"

release-frontend-dev:
	@$(MAKE) --no-print-directory release-frontend-core ENV_FILE=.env.k3s-dev VERSION_MODE=latest MODE_LABEL="Frontend Release (dev)"

release-frontend-core:
	@set -e; \
	upsert_image_key() { \
		key="$$1"; value="$$2"; file="$$3"; \
		if grep -q "^$$key=" "$$file"; then \
			sed -i '' "s|^$$key=.*|$$key=$$value|" "$$file"; \
		else \
			if grep -q '^# Docker Image' "$$file"; then \
				awk -v k="$$key" -v v="$$value" '\
					BEGIN { inDocker=0; inserted=0 } \
					{ \
						if ($$0 ~ /^# Docker Image/) { inDocker=1; print; next } \
						if (inDocker && ($$0 ~ /^# / || $$0 ~ /^$$/)) { \
							if (!inserted) { print k "=" v; inserted=1 } \
							inDocker=0; \
						} \
						print; \
					} \
					END { if (!inserted) { print k "=" v } }' "$$file" > "$$file.tmp" && mv "$$file.tmp" "$$file"; \
			else \
				printf "# Docker Image\n%s=%s\n" "$$key" "$$value" >> "$$file"; \
			fi; \
		fi; \
	}; \
	CURRENT_IMG=$$(grep '^FRONTEND_IMAGE=' "$(ENV_FILE)" | head -1 | cut -d= -f2- || true); \
	if [ -n "$$CURRENT_IMG" ] && echo "$$CURRENT_IMG" | grep -q ':'; then REPO=$${CURRENT_IMG%:*}; else REPO="$$CURRENT_IMG"; fi; \
	if [ -z "$$REPO" ]; then \
		read -p "FRONTEND_IMAGE가 비어 있습니다. 이미지 경로(태그 제외)를 입력하세요: " REPO; \
		if [ -z "$$REPO" ]; then echo "이미지 경로를 입력해주세요."; exit 1; fi; \
	fi; \
	if [ "$(VERSION_MODE)" = "latest" ]; then \
		VERSION=latest; \
		echo "dev는 latest 태그를 사용합니다: $$REPO:$$VERSION"; \
	else \
		echo "현재 버전: $$CURRENT_IMG"; \
		echo ""; \
		read -p "새 버전을 입력하세요 (예: 0.0.1): " VERSION; \
		if [ -z "$$VERSION" ]; then echo "버전을 입력해주세요."; exit 1; fi; \
	fi; \
	echo ""; \
	echo "=== $(MODE_LABEL) ==="; \
	echo "[1/2] Frontend build..."; \
	cd web-client && npm run build && cd .. && \
	echo "[2/2] Docker build & push: $$REPO:$$VERSION" && \
	docker buildx build --platform linux/amd64,linux/arm64 -t $$REPO:$$VERSION --push web-client/ && \
	upsert_image_key "FRONTEND_IMAGE" "$$REPO:$$VERSION" "$(ENV_FILE)" && \
	echo "" && \
	echo "Frontend release 완료: $$REPO:$$VERSION" && \
	echo "   $(ENV_FILE) 업데이트 완료"
