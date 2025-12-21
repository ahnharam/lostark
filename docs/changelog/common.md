# 공통 변경 이력

> **대상**: 라우팅, 레이아웃, 공통 컴포넌트, 문서화
> **관련 컴포넌트**: `MainLayout.vue`, `router/index.ts`, `components/common/*`

---

## 2025-12

### 2025-12-21: 문서 정리 규칙 수립

**변경 유형**: Documentation

**변경 내용**:
1. **문서 명명 규칙 도입**
   - 카테고리별 접두사: `guide-`, `phase{N}-`, `report-`, `ref-`
   - kebab-case 파일명 규칙

2. **Changelog 폴더 구조 도입**
   - `docs/changelog/` 폴더 생성
   - 메인메뉴별 변경 이력 분리

3. **AI 세션 가이드 추가**
   - 필독 문서 순서 명시
   - 문서 작성 위치 결정 흐름도

**영향받은 파일**:
- `docs/documentation-guidelines.md`
- `docs/README.md`
- `docs/dev-quickstart.md`
- `docs/changelog/*` (신규)

---

## 2025-01

### 2025-01: Phase 4 라우팅 구조 개선

**변경 유형**: Refactor

**변경 내용**:
1. **라우터 재설계** (`router/index.ts`)
   - 단일 라우트 (`/:menu?`) → 중첩 라우트 구조
   - 15+ 라우트 정의 (각 메뉴별 독립 URL)
   - route.meta를 통한 메뉴/서브메뉴 추적
   - 레거시 URL 자동 리다이렉트 구현

2. **MainLayout 업데이트**
   - Dynamic component (`<component :is="activeComponent">`) → `<router-view>`
   - componentMap 제거
   - activeMenu: ref → computed (route.meta 기반)
   - 네비게이션: 직접 상태 변경 → router.push

3. **메뉴 컴포넌트 분리**
   - **AuctionMenu**: 거래소 로직을 `auction/MarketView.vue`로 분리 (1,500줄)
   - **RaidMenu**: router-view wrapper로 간소화
   - **AdminMenu**: route.meta 기반 헤더 동적 변경
   - **ReforgeMenu**: activeSubMenuTab을 computed로 변경

**성과**:
- 딥링킹 지원 (`/auction/market`, `/reforge/blunt-thorn` 등)
- 브라우저 뒤로/앞으로 버튼 정상 작동
- URL 공유 가능 (북마크, 외부 링크)
- 레거시 URL 호환성 (쿼리 파라미터 보존)

**영향받은 파일**:
- `frontend/src/router/index.ts`
- `frontend/src/components/MainLayout.vue`
- `frontend/src/components/AuctionMenu.vue`
- `frontend/src/components/RaidMenu.vue`
- `frontend/src/components/AdminMenu.vue`
- `frontend/src/components/ReforgeMenu.vue`

**관련 문서**: `docs/phase4-routing-guide.md`
