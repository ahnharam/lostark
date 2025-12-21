# 재련 변경 이력

> **대상 메뉴**: 일반/상급 재련, 뭉가 계산기, 음돌 계산기
> **관련 컴포넌트**: `ReforgeMenu.vue`, `reforge/BluntThornCalculator.vue`, `reforge/SupersonicCalculator.vue`

---

## 2025-01

### 2025-01: Phase 4 라우팅 개선

**변경 유형**: Refactor

**변경 내용**:
- `activeSubMenuTab`을 computed로 변경 (route.meta 기반)
- 독립 URL 지원 (`/reforge/normal`, `/reforge/advanced`, `/reforge/blunt-thorn`, `/reforge/supersonic`)

**영향받은 파일**:
- `frontend/src/components/ReforgeMenu.vue`

**관련 문서**: `docs/phase4-routing-guide.md`
