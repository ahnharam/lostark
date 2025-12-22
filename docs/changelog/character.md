# 캐릭터 검색 변경 이력

> **대상 메뉴**: 캐릭터 검색, 프로필, 원정대, 통합 메뉴
> **관련 컴포넌트**: `CharacterSearch.vue`, `characterStore.ts`, `composables/character/*`

---

## 2025-12

### 2025-12-22: 원정대 메뉴에 주간 골드 획득 기능 추가

**변경 유형**: Feature

**변경 내용**:
- 캐릭터 검색 → 원정대 탭에 주간 골드 획득 패널 추가
- 캐릭터별 레이드 완료 체크박스로 주간 골드 자동 계산
- 레이드 난이도별 골드 보상 정보 표시 (2025년 기준)
- 선택된 캐릭터들의 총 골드 합계 실시간 계산
- 아이템 레벨에 따른 참여 가능 레이드 자동 필터링

**추가된 파일**:
- `frontend/src/api/types/weekly-gold.ts`: 주간 골드 타입 정의
- `frontend/src/composables/character/useWeeklyGoldData.ts`: 주간 골드 데이터 로직
- `frontend/src/components/character/WeeklyGoldPanel.vue`: 주간 골드 UI 컴포넌트

**수정된 파일**:
- `frontend/src/components/CharacterSearch.vue`: WeeklyGoldPanel 통합

**주요 기능**:
1. **레이드 체크박스**: 각 캐릭터별로 완료한 레이드를 체크
2. **자동 골드 계산**: 체크된 레이드 기준으로 획득 골드 자동 합산
3. **캐릭터 선택**: 특정 캐릭터만 선택하여 부분 합계 계산
4. **확장/축소**: 패널 접기/펼치기 기능으로 공간 절약
5. **다크모드 지원**: 전체 UI 다크모드 대응
6. **자동 정렬**: 아이템 레벨 내림차순으로 캐릭터 정렬
7. **자동 선택**: 상위 6캐릭터만 자동 체크 (주간 골드 획득 가능 캐릭터 수)

**UI/UX**:
- 주간 골드 획득 패널이 원정대 보유 캐릭터 목록보다 위에 배치
- 캐릭터 리스트는 아이템 레벨 기준 내림차순 정렬
- 레벨이 높은 상위 6캐릭터만 기본 선택 상태로 초기화

---

### 2025-12-22: 주간 골드 획득 패널 개선

**변경 유형**: Feature / Fix

**변경 내용**:
- 레이드 카탈로그 기반 난이도별 입장 레벨/골드 적용
- 약어 우선 표기(없으면 레이드 이름 사용)
- 캐릭터 레벨 기준 자동 선택(최대 3개, 높은 레벨 우선, 약어 중복 제거)
- 서버별 그룹/접기 UI 추가
- 캐릭터/레이드 변경 시 주간 골드 목록 자동 갱신

**영향받은 파일**:
- `frontend/src/composables/character/useWeeklyGoldData.ts`
- `frontend/src/components/character/WeeklyGoldPanel.vue`
- `frontend/src/api/types/weekly-gold.ts`

---

### 2025-12-22: 아크패시브 랭크값 빈 값 처리

**변경 유형**: Fix

**변경 내용**:
- 아크패시브 영역에서 Description 값이 빈 문자열("")이거나 없을 경우 "미개방" 표시
- 통합 메뉴 → 아크패시브 헤더의 랭크값 표출 로직 개선
- 조건부 렌더링 수정으로 빈 값일 때 영역이 사라지는 문제 해결

**영향받은 파일**:
- `frontend/src/components/common/CharacterSummaryPanel.vue`

---

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
