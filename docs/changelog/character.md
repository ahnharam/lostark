# 캐릭터 검색 변경 이력

> **대상 메뉴**: 캐릭터 검색, 프로필, 원정대, 통합 메뉴
> **관련 컴포넌트**: `CharacterSearch.vue`, `characterStore.ts`, `composables/character/*`

---

## 2025-12

### 2025-12-21: Phase 5 성능 최적화 + 버그 수정

**변경 유형**: Performance / Fix

**변경 내용**:
1. **Pinia 스토어 도입 (`characterStore.ts`)**
   - 캐릭터 상태 관리 중앙화
   - Promise.allSettled 기반 통합 메뉴 데이터 병렬 로드
   - 데이터 로딩 실패 시 재시도 로직 (최대 3회, 1초 간격)
   - 검색 시 이전 캐릭터 데이터 완전 초기화

2. **버그 수정**
   - API 중복 호출 문제 해결 (watch에서 summary 탭 중복 로드 제거)
   - X 버튼 클릭 시 검색 패널 표시 수정 (`@click.stop` + `emit('focus')`)
   - 아크패시브 티어 정렬 수정 (오름차순)
   - 최근 검색 항목 5개 제한

**성과**:
- 초기 로딩 시간 20-30% 단축
- 통합 메뉴 데이터 즉시 표시
- 안정적인 데이터 로딩

**영향받은 파일**:
- `frontend/src/stores/characterStore.ts` (신규)
- `frontend/src/components/CharacterSearch.vue`
- `frontend/src/components/common/AutocompleteInput.vue`
- `frontend/src/composables/character/useArkGridData.ts`

**관련 문서**: `docs/phase5-performance-optimization-plan.md`

---

## 2024-12

### 2024-12: Phase 2-3 컴포넌트 리팩토링

**변경 유형**: Refactor

**변경 내용**:
1. **Composables 추출** (총 5개, 1,781+ 줄 분리)
   - `useExpeditionData.ts` (105줄): 원정대 캐릭터 서버별 그룹화/정렬
   - `useCollectibleData.ts` (81줄): 수집품 진행도 계산 및 포맷팅
   - `useArkGridData.ts` (360+줄): 아크 패시브/코어/포인트 데이터 변환
   - `useEquipmentData.ts` (615줄): 장비/아바타/각인 데이터 요약
   - `useSkillData.ts` (620줄): 스킬/보석/룬 데이터 처리

2. **UI 컴포넌트 추출** (총 4개)
   - `CharacterSearchPanel.vue`: 검색 입력, 자동완성, 기록/즐겨찾기
   - `ExpeditionCharacterList.vue`: 원정대 캐릭터 그리드
   - `CharacterResultTabs.vue`: 결과 탭 네비게이션
   - `CharacterResultHeader.vue`: 캐릭터 헤더 (프로필, 새로고침, 즐겨찾기)

**성과**:
- CharacterSearch.vue 크기 1,760줄 감소 (5,460줄 → 3,700줄)
- 비즈니스 로직 재사용 가능
- 타입 안전성 향상

**관련 문서**: `docs/phase2-3-completion-report.md`

---

## 2024-11

### 2024-11: Phase 1 데이터 변환 유틸리티

**변경 유형**: Refactor

**변경 내용**:
- **Transform 유틸리티 생성** (총 4개, `utils/character/` 폴더)
  - `skillDataTransform.ts` (10,182바이트): 스킬 데이터 변환 로직
  - `equipmentDataTransform.ts` (12,875바이트): 장비/아바타 데이터 변환
  - `arkGridDataTransform.ts` (7,948바이트): 아크 그리드 데이터 변환
  - `cardDataTransform.ts` (4,981바이트): 카드 데이터 변환

**성과**:
- 데이터 변환 로직 중앙화
- 타입 안전성 확보
- Phase 2-3 Composable 작업의 기반 마련
