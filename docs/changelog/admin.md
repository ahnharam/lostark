# 관리자 변경 이력

> **대상 메뉴**: 거래소 통계, 레이드 카탈로그
> **관련 컴포넌트**: `AdminMenu.vue`, `AdminStats.vue`, `AdminRaidCatalog.vue`

---

## 2025-01

### 2025-01: Phase 4 라우팅 개선

**변경 유형**: Refactor

**변경 내용**:
- route.meta 기반 헤더 동적 변경
- 독립 URL 지원 (`/admin/market-records`, `/admin/raid-catalog`)

**영향받은 파일**:
- `frontend/src/components/AdminMenu.vue`

**관련 문서**: `docs/phase4-routing-guide.md`
