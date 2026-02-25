#!/bin/bash

# 배포 가이드 기준 사용 가능한 모듈 목록
VALID_MODULES=("web-client" "api-gateway" "settlement" "member" "payment" "order" "product" "file" "inventory")

echo "Modeunsa 멀티 모듈 태그 자동 생성 스크립트"
echo "================================================="
echo "팁: 여러 모듈을 배포하려면 띄어쓰기로 구분해서 순서대로 주세요."
echo "주의: 'member' 모듈을 포함하여 배포할 경우, 반드시 맨 마지막 순서로 입력해 주세요!"
echo "사용 가능한 모듈: ${VALID_MODULES[*]}"
echo "-------------------------------------------------"

# 1. 모듈 입력 받기 (띄어쓰기로 구분)
read -p "배포할 모듈 이름들을 순서대로 입력하세요: " -a MODULE_ARRAY

if [ ${#MODULE_ARRAY[@]} -eq 0 ]; then
  echo "모듈이 입력되지 않았습니다. 스크립트를 종료합니다."
  exit 1
fi

echo "================================================="
echo "각 모듈의 버전을 개별적으로 입력받습니다..."
echo "================================================="

# 2. 각 모듈을 순회하며 개별 버전 입력 및 태그 푸시
for MODULE in "${MODULE_ARRAY[@]}"; do
  # 입력한 모듈이 유효한 모듈 목록에 있는지 검사
  if [[ " ${VALID_MODULES[*]} " =~ " ${MODULE} " ]]; then

    # 해당 모듈의 버전만 따로 입력받기
    read -p "[$MODULE] 모듈의 새로운 버전을 입력하세요 (예: 1.0.3): " VERSION

    if [[ -z "$VERSION" ]]; then
      echo "버전이 입력되지 않아 [$MODULE] 배포를 건너뜁니다."
      echo "-------------------------------------------------"
      continue
    fi

    TAG_NAME="${MODULE}-v${VERSION}"
    echo "[$MODULE] 태그 생성 및 푸시 중: $TAG_NAME"

    # 태그 생성
    git tag "$TAG_NAME"

    # 깃허브로 태그 푸시
    if git push origin "$TAG_NAME"; then
      echo "[$MODULE] 성공적으로 푸시되었습니다!"
    else
      echo "[$MODULE] 푸시 실패! 이미 존재하는 태그이거나 권한 문제인지 확인해 주세요."
    fi
    echo "-------------------------------------------------"
  else
    echo "경고: '$MODULE'은(는) 배포 가이드에 명시된 유효한 모듈명이 아닙니다. 오타를 확인해 주세요. (건너뜁니다)"
    echo "-------------------------------------------------"
  fi
done

echo "모든 작업이 완료되었습니다! Github Actions 탭에서 배포 진행 상황을 확인해 보세요."