# 경매장/거래소 변경 이력

> **대상 메뉴**: 거래소, 경매장
> **관련 컴포넌트**: `AuctionMenu.vue`, `auction/MarketView.vue`, `auction/AuctionHouseView.vue`

---

## 2025-12

### 2025-12-22: 거래소 필터/정렬 UX 개선

**변경 유형**: Feature / UX

**변경 내용**:
- 메인 카테고리 탭 정리 및 기본 선택값 조정(전체 버튼 숨김)
- 카테고리 "전체보기" 시 다중 카테고리 코드 검색 지원
- 정렬 UI를 헤더로 이동하고 오름/내림 토글 추가
- 등락률 컬럼 추가(현재 최저가 vs 전일 평균) 및 색상 표시
- 카테고리별 등급/티어 기본값 및 옵션 제한 규칙 반영

**영향받은 파일**:
- `frontend/src/components/auction/MarketView.vue`
- `frontend/src/api/lostark.ts`
- `frontend/src/api/types/market.ts`

---

## 2025-01

### 2025-01: Phase 4 라우팅 분리

**변경 유형**: Refactor

**변경 내용**:
- 거래소 로직을 `auction/MarketView.vue`로 분리 (1,500줄)
- `AuctionMenu.vue`를 router-view wrapper로 변경
- 독립 URL 지원 (`/auction/market`, `/auction/auction-house`)

**영향받은 파일**:
- `frontend/src/components/AuctionMenu.vue`
- `frontend/src/components/auction/MarketView.vue` (신규)
- `frontend/src/components/auction/AuctionHouseView.vue` (신규)

**관련 문서**: `docs/phase4-routing-guide.md`
