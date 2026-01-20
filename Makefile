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

web-client-docker-build:
	@echo "Building web client..."
	@docker-compose -f docker-compose-dev.yml build web-client
	@echo "Web client build complete."

dev-docker-run:
	@echo "Starting development environment using Docker..."
	@docker-compose -f docker-compose-dev.yml up -d

dev-docker-down:
	@echo "Starting development environment using Docker..."
	@docker-compose -f docker-compose-dev.yml down