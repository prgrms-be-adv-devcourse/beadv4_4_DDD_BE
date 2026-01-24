# =========================
# Env loading
# =========================
ifneq (,$(wildcard .env))
	include .env
	export
endif

git-setup: git-template git-hooks
	@echo "✅ Done. (repo-local git template + hooks applied)"

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

docker-build:
	@echo "Gradle clean & build"
	./gradlew clean build

	@echo "Building image: $(DOCKER_IMAGE)"
	docker build --platform linux/arm64 -t $(DOCKER_IMAGE) .

docker-push:
	@echo "Pushing image: $(DOCKER_IMAGE)"
	docker push $(DOCKER_IMAGE)