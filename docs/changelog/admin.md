# 관리자 변경 이력

> **대상 메뉴**: 거래소 통계, 레이드 카탈로그
> **관련 컴포넌트**: `AdminMenu.vue`, `AdminStats.vue`, `AdminRaidCatalog.vue`

---

## 2025-12

### 2025-12-22: 레이드 관리 기능 확장

**변경 유형**: Feature

**변경 내용**:
- 관리자 메뉴 명칭을 "레이드 관리"로 변경
- 레이드 약어, 난이도별 입장 레벨/골드, 인원 설정 UI 추가
- 등록된 레이드 목록에서 수정/삭제 지원

**영향받은 파일**:
- `frontend/src/components/AdminMenu.vue`
- `frontend/src/components/MainLayout.vue`
- `frontend/src/components/AdminRaidCatalog.vue`
- `frontend/src/api/lostark.ts`

---

### 2025-12-22: 레이드 관리 일괄 수정 모드 개선

**변경 유형**: Feature / Fix

**변경 내용**:
- 등록된 레이드 목록 편집을 상단 일괄 수정/저장/취소로 전환
- 수정 모드 진입 시 골드 입력값이 비어 보이던 문제 수정

**영향받은 파일**:
- `frontend/src/components/AdminRaidCatalog.vue`

## 2025-01

### 2025-01: Phase 4 라우팅 개선

**변경 유형**: Refactor

**변경 내용**:
- route.meta 기반 헤더 동적 변경
- 독립 URL 지원 (`/admin/market-records`, `/admin/raid-catalog`)

**영향받은 파일**:
- `frontend/src/components/AdminMenu.vue`

**관련 문서**: `docs/phase4-routing-guide.md`
