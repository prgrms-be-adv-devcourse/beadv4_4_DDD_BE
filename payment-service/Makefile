TARGET_BACKUP_DIR=./bin/backup

BUILD_NUM=$$(cat ./deploy/build_num.txt)
BUILD_NUM_FILE=./build_num.txt
VERSION_NUM=$$(cat ./deploy/version.txt)

TARGET_VERSION=$(VERSION_NUM).$(BUILD_NUM)

MODULE_NAME=payment-service

DOCKER_REPOSITORY=kjuiop

DOCKER_IMAGE=$(DOCKER_REPOSITORY)/$(MODULE_NAME):$(TARGET_VERSION)

all: config target-version boot_jar multi-docker-build-push

config:
	@if [ ! -d $(TARGET_DIR) ]; then mkdir $(TARGET_DIR); fi

build_num:
	@echo $$(($$(cat $(BUILD_NUM_FILE)) + 1 )) > $(BUILD_NUM_FILE)

boot_jar:
	../gradlew clean :$(MODULE_NAME):bootjar

docker_push:
	@echo "TARGET_VERSION : $(TARGET_VERSION)"
	docker push $(DOCKER_REPOSITORY)/hs-$(ADMIN_MODULE_NAME):$(TARGET_VERSION)


target-version:
	@echo "========================================"
	@echo "APP_VERSION    : $(VERSION_NUM)"
	@echo "BUILD_NUM      : $(BUILD_NUM)"
	@echo "TARGET_VERSION : $(TARGET_VERSION)"
	@echo "========================================"

multi-docker-build-push:
	docker buildx build \
      --platform linux/amd64,linux/arm64 \
      -t $(DOCKER_IMAGE) \
      --push .