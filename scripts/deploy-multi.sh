#!/bin/bash

# 배포 가이드 기준 사용 가능한 모듈 목록
VALID_MODULES=("web-client" "api-gateway" "settlement" "member" "payment" "order" "product" "file" "inventory")

echo "Modeunsa 멀티 모듈 태그 자동 생성 스크립트"
echo "================================================="
echo "팁: 여러 모듈을 배포하려면 띄어쓰기로 구분해 주세요."
echo "주의: 'member' 모듈이 포함될 경우, 스크립트가 자동으로 가장 마지막 순서로 재정렬합니다."
echo "사용 가능한 모듈: ${VALID_MODULES[*]}"
echo "-------------------------------------------------"

# 1. 모듈 입력 받기
read -p "배포할 모듈 이름들을 입력하세요: " -a INPUT_ARRAY

if [ ${#INPUT_ARRAY[@]} -eq 0 ]; then
  echo "모듈이 입력되지 않았습니다. 스크립트를 종료합니다."
  exit 1
fi

# 2. 유효성 검사 및 'member' 모듈 순서 자동 강제 정렬 로직
MODULE_ARRAY=()
HAS_MEMBER=false

for MODULE in "${INPUT_ARRAY[@]}"; do
  # 유효한 모듈명인지 검사
  if [[ ! " ${VALID_MODULES[*]} " =~ " ${MODULE} " ]]; then
    echo "경고: '$MODULE'은(는) 유효한 모듈명이 아닙니다. 제외합니다."
    continue
  fi

  # member 모듈은 따로 빼두고 나머지 모듈만 배열에 먼저 담기
  if [ "$MODULE" == "member" ]; then
    HAS_MEMBER=true
  else
    MODULE_ARRAY+=("$MODULE")
  fi
done

# member 모듈이 있었다면 배열의 맨 마지막에 추가
if [ "$HAS_MEMBER" == "true" ]; then
  MODULE_ARRAY+=("member")
  echo "알림: 'member' 모듈이 포함되어 있어 안전한 배포를 위해 자동으로 마지막 순서로 재정렬되었습니다."
fi

# 유효한 모듈이 하나도 안 남았다면 종료
if [ ${#MODULE_ARRAY[@]} -eq 0 ]; then
  echo "유효한 배포 대상 모듈이 없어 스크립트를 종료합니다."
  exit 1
fi

echo "================================================="
echo "최종 배포 순서: ${MODULE_ARRAY[*]}"
echo "각 모듈의 버전을 개별적으로 입력받습니다..."
echo "================================================="

# 3. 각 모듈을 순회하며 개별 버전 입력 및 태그 푸시
for MODULE in "${MODULE_ARRAY[@]}"; do
  read -p "[$MODULE] 모듈의 새로운 버전을 입력하세요 (예: 1.0.3): " VERSION

  if [[ -z "$VERSION" ]]; then
    echo "버전이 입력되지 않아 [$MODULE] 배포를 건너뜁니다."
    echo "-------------------------------------------------"
    continue
  fi

  TAG_NAME="${MODULE}-v${VERSION}"
  echo "[$MODULE] 태그 처리 중: $TAG_NAME"

  # 로컬에 태그가 이미 존재하는지 사전 검증
  if git rev-parse -q --verify "refs/tags/$TAG_NAME" >/dev/null; then
    echo "[$MODULE] 로컬에 이미 '$TAG_NAME' 태그가 존재합니다. 버전을 올리거나 기존 태그를 삭제해 주세요. (건너뜁니다)"
    echo "-------------------------------------------------"
    continue
  fi

  # 태그 생성 시도 및 Exit Code 확인
  if ! git tag "$TAG_NAME"; then
    echo "[$MODULE] git tag 생성 명령어 실행에 실패했습니다. 상태를 확인해 주세요. (건너뜁니다)"
    echo "-------------------------------------------------"
    continue
  fi

  # 깃허브로 태그 푸시
  echo "원격 저장소로 푸시 중..."
  if git push origin "$TAG_NAME"; then
    echo "[$MODULE] 성공적으로 푸시되었습니다! ($TAG_NAME)"
  else
    # 원격 푸시 실패 시(권한 등) 꼬이지 않도록 방금 만든 로컬 태그 삭제 롤백
    echo "[$MODULE] 원격 푸시 실패! 네트워크 상태나 권한을 확인해 주세요."
    git tag -d "$TAG_NAME" >/dev/null 2>&1
    echo "실패한 로컬 태그($TAG_NAME)를 롤백(삭제)했습니다."
  fi
  echo "-------------------------------------------------"
done

echo "모든 작업이 완료되었습니다! Github Actions 탭에서 배포 진행 상황을 확인해 보세요."