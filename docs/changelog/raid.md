# 레이드 변경 이력

> **대상 메뉴**: 레이드 파티 관리
> **관련 컴포넌트**: `RaidMenu.vue`, `RaidPartyManager.vue`

---

## 2025-01

### 2025-01: Phase 4 라우팅 개선

**변경 유형**: Refactor

**변경 내용**:
- `RaidMenu.vue`를 router-view wrapper로 간소화
- 독립 URL 지원 (`/raid/party`)

**영향받은 파일**:
- `frontend/src/components/RaidMenu.vue`

**관련 문서**: `docs/phase4-routing-guide.md`
