# =========================
# Env loading
# =========================
ifneq (,$(wildcard .env))
	include .env
	export
endif

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
# Backend: Build & Push
# =========================
release-backend:
	@set -e; \
	CURRENT_IMG=$$(grep '^DOCKER_IMAGE=' .env.k3s-prod | head -1 | cut -d= -f2); \
	CURRENT_TAG=$$(echo "$$CURRENT_IMG" | sed 's/.*://'); \
	REPO=$$(echo "$$CURRENT_IMG" | sed 's/:.*//'); \
	echo ""; \
	echo "=== Backend Release ==="; \
	echo "현재 버전: $$CURRENT_IMG"; \
	echo ""; \
	read -p "새 버전을 입력하세요 (예: 0.0.1): " VERSION; \
	if [ -z "$$VERSION" ]; then echo "버전을 입력해주세요."; exit 1; fi; \
	echo ""; \
	echo "[1/2] Gradle clean build..."; \
	./gradlew clean build -x test && \
	echo "[2/2] Docker build & push: $$REPO:$$VERSION" && \
	docker buildx build --platform linux/amd64,linux/arm64 -t $$REPO:$$VERSION --push . && \
	sed -i '' "s|DOCKER_IMAGE=.*|DOCKER_IMAGE=$$REPO:$$VERSION|" .env.k3s-prod && \
	echo "" && \
	echo "Backend release 완료: $$REPO:$$VERSION" && \
	echo "   .env.k3s-prod 업데이트 완료"

# =========================
# Frontend: Build & Push
# =========================
release-frontend:
	@set -e; \
	CURRENT_IMG=$$(grep '^FRONTEND_IMAGE=' .env.k3s-prod | head -1 | cut -d= -f2); \
	CURRENT_TAG=$$(echo "$$CURRENT_IMG" | sed 's/.*://'); \
	REPO=$$(echo "$$CURRENT_IMG" | sed 's/:.*//'); \
	echo ""; \
	echo "=== Frontend Release ==="; \
	echo "현재 버전: $$CURRENT_IMG"; \
	echo ""; \
	read -p "새 버전을 입력하세요 (예: 0.0.1): " VERSION; \
	if [ -z "$$VERSION" ]; then echo "버전을 입력해주세요."; exit 1; fi; \
	echo ""; \
	echo "[1/2] Frontend build..."; \
	cd web-client && npm run build && cd .. && \
	echo "[2/2] Docker build & push: $$REPO:$$VERSION" && \
	docker buildx build --platform linux/amd64,linux/arm64 -t $$REPO:$$VERSION --push web-client/ && \
	sed -i '' "s|FRONTEND_IMAGE=.*|FRONTEND_IMAGE=$$REPO:$$VERSION|" .env.k3s-prod && \
	echo "" && \
	echo "Frontend release 완료: $$REPO:$$VERSION" && \
	echo "   .env.k3s-prod 업데이트 완료"