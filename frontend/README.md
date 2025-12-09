# 프론트엔드 안내 (Vue 3 + Vite)

## 실행
```bash
cd frontend
npm install          # 최초 1회
npm run dev          # 로컬 HMR
# 원격/전체 바인딩: npm run dev:host
# 컨테이너로 실행: docker compose up -d frontend
```

## 주요 경로
- 진입: `src/main.ts` → `App.vue` → `components/MainLayout.vue`
- 화면:  
  - 경매: `components/AuctionMenu.vue`  
  - 관리자 통계: `components/AdminStats.vue` (검색/페이징/수동 캡처/수집 중 오버레이)  
  - 캐릭터 검색: `components/CharacterSearch.vue`  
  - 아크 그리드: `components/common/ArkGridPanel.vue` (연동 부분)
- 라우터: `src/router/index.ts` (`/:menu?` 단일 라우트)
- API 래퍼: `src/api/lostark.ts`, 타입: `src/api/types/`

## 스크립트
- `npm run dev` / `npm run dev:host`
- `npm run build`
- `npm run lint`
- `npm run type-check`

## 상태/UX
- 서버 상태 배지: `MainLayout.vue` ( `/api/markets/options` 헬스 기반 초록/빨강 )
- 관리자 통계 화면: 수집 중 오버레이, 페이지/검색 가능

## 추가 문서
- `frontend/docs/UX_OVERVIEW.md`
- `frontend/docs/CHARACTER_RANKING_GUIDE.md`
- `frontend/docs/ARK_GRID_GUIDE.md`
