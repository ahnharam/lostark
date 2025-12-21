# 경매장/거래소 변경 이력

> **대상 메뉴**: 거래소, 경매장
> **관련 컴포넌트**: `AuctionMenu.vue`, `auction/MarketView.vue`, `auction/AuctionHouseView.vue`

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
