# Web Client (Next.js)

주문 및 결제 페이지를 위한 Next.js 웹 클라이언트입니다.

## 시작하기

### 1. 의존성 설치

```bash
npm install
# 또는
yarn install
# 또는
pnpm install
```

### 2. 개발 서버 실행

```bash
npm run dev
# 또는
yarn dev
# 또는
pnpm dev
```

개발 서버가 실행되면 [http://localhost:3000](http://localhost:3000)에서 애플리케이션을 확인할 수 있습니다.

### 3. 빌드 및 프로덕션 실행

```bash
# 빌드
npm run build

# 프로덕션 서버 실행
npm start
```

## 프로젝트 구조

```
web-client/
├── app/
│   ├── layout.tsx      # 루트 레이아웃
│   ├── page.tsx        # 메인 페이지 (주문 페이지)
│   └── globals.css     # 전역 스타일
├── next.config.js      # Next.js 설정
├── package.json        # 프로젝트 의존성
└── tsconfig.json       # TypeScript 설정
```

## 주요 기능

- 주문서 정보 표시
- 결제 금액 계산 및 표시
- 주문 상품 정보 표시
- 결제 수단 선택
- 약관 동의 안내
- 결제 버튼

## 기술 스택

- **Next.js 14** - React 프레임워크
- **TypeScript** - 타입 안정성
- **CSS** - 스타일링
