# Utils 추출 리팩토링 가이드

## 개요

이 문서는 CharacterSearch.vue와 같은 비대한 컴포넌트에서 순수 함수를 추출하여 `src/utils/` 디렉토리로 분리하는 과정을 설명합니다.

## 완성된 예시: skillDataTransform.ts

[frontend/src/utils/character/skillDataTransform.ts](../frontend/src/utils/character/skillDataTransform.ts)

이 파일은 CharacterSearch.vue에서 300줄의 스킬 관련 순수 함수를 추출한 예시입니다.

## 추출 프로세스

### 1단계: 추출할 함수 식별

#### 식별 기준
✅ **추출해야 하는 함수:**
- 순수 함수 (side effect 없음)
- 데이터 변환/파싱 로직
- 여러 곳에서 재사용 가능
- Vue 컴포넌트 의존성 없음 (ref, computed 사용 안함)

❌ **추출하면 안되는 함수:**
- ref, computed, watch 등 Vue Composition API 사용
- 컴포넌트 상태에 의존
- API 호출 포함

#### 명령어로 함수 찾기
```bash
# CharacterSearch.vue에서 특정 패턴의 함수 찾기
grep -n "^const.*= (" frontend/src/components/CharacterSearch.vue
```

### 2단계: 파일 구조 설계

#### 디렉토리 구조
```
src/utils/
├── character/
│   ├── skillDataTransform.ts      # 스킬 관련
│   ├── equipmentDataTransform.ts  # 장비 관련
│   ├── arkGridDataTransform.ts    # 아크 관련
│   └── cardDataTransform.ts       # 카드 관련
└── auction/
    ├── marketDataTransform.ts     # 거래소 관련
    └── auctionFilters.ts          # 필터 관련
```

#### 파일 명명 규칙
- **도메인명 + 기능 + .ts** 형식
- 예: `skillDataTransform.ts`, `equipmentDataTransform.ts`
- 카멜케이스 사용

### 3단계: 파일 템플릿

```typescript
/**
 * [기능] 데이터 변환 유틸리티
 *
 * [컴포넌트명]에서 추출한 순수 함수들입니다.
 * [간단한 설명].
 */

import type { /* API 타입 */ } from '@/api/types/...'
import { /* 기존 유틸 */ } from '@/utils/...'

// ============================================================================
// Types
// ============================================================================

export type MyCustomType = '...' | '...'

export interface MyInterface {
  // ...
}

// ============================================================================
// Constants
// ============================================================================

export const MY_CONSTANT_REGEX = /pattern/i

// ============================================================================
// Helper Functions (Internal) - export 하지 않음
// ============================================================================

/**
 * 내부 헬퍼 함수 (private)
 */
const internalHelper = (value: string): string => {
  // ...
}

// ============================================================================
// Core Functions - export 필수
// ============================================================================

/**
 * 함수 설명
 *
 * @param param1 - 파라미터 설명
 * @param param2 - 파라미터 설명
 * @returns 반환값 설명
 *
 * @example
 * myFunction('input') // 'output'
 */
export const myFunction = (param1: string, param2?: number): string => {
  // 구현
}
```

### 4단계: 의존성 처리

#### 필요한 import 확인

**기존 컴포넌트에서 사용하던 함수들:**
- `inlineText()` → 직접 구현 또는 `utils/tooltipText.ts`에서 import
- `isRecord()` → `utils/typeGuards.ts`에서 import
- `flattenTooltipLines()` → `utils/tooltipText.ts`에서 import

#### import 예시
```typescript
// API 타입
import type { CombatSkill, SkillGem } from '@/api/types/skills'

// 기존 유틸
import { sanitizeInline, flattenTooltipLines } from '@/utils/tooltipText'
import { isRecord, isString } from '@/utils/typeGuards'
```

### 5단계: 함수 추출 및 문서화

#### JSDoc 작성 규칙
```typescript
/**
 * [한 줄 요약]
 *
 * [상세 설명 - 선택사항]
 * - 리스트 아이템
 * - 리스트 아이템
 *
 * @param param1 - 파라미터 설명
 * @param param2 - 선택적 파라미터 설명 (선택사항)
 * @returns 반환값 설명
 *
 * @example
 * functionName('input') // 'output'
 * functionName('foo', 'bar') // 'baz'
 */
export const functionName = (param1: string, param2?: string): string => {
  // ...
}
```

#### 타입 안전성
- **모든 파라미터에 타입 지정**
- **반환 타입 명시**
- **제네릭은 필요시만 사용**

```typescript
// ✅ Good
export const normalize = (value?: string | null): string => {
  return value?.trim().toLowerCase() ?? ''
}

// ❌ Bad - 타입 없음
export const normalize = (value) => {
  return value?.trim().toLowerCase() ?? ''
}
```

### 6단계: 원본 컴포넌트 수정

#### 6-1. Import 추가
```typescript
// CharacterSearch.vue의 <script setup> 상단에 추가
import {
  normalizeSkillKey,
  extractGemLabel,
  classifyGemEffect,
  isAwakeningSkill,
  // ... 기타 함수들
  type GemEffectPlacement,
  type SkillGemSlot
} from '@/utils/character/skillDataTransform'
```

#### 6-2. 기존 함수 정의 제거
```typescript
// ❌ 제거할 코드
const normalizeSkillKey = (value?: string | null) =>
  inlineText(value)
    .replace(/[\s\[\]\(\)<>{}]/g, '')
    .toLowerCase()

// ❌ 제거할 코드
const extractGemLabel = (gem: SkillGem) => {
  // ... 50줄
}
```

#### 6-3. 함수 사용은 그대로
```typescript
// ✅ 그대로 사용 가능 (import 했으므로)
const key = normalizeSkillKey(skill.name)
const label = extractGemLabel(gem)
```

### 7단계: 검증

#### 타입 체크
```bash
cd frontend
npm run type-check
```

**예상 결과:**
- 타입 오류 없음
- import 오류 없음

#### 기능 테스트
```bash
cd frontend
npm run dev
```

**테스트 항목:**
- [ ] 캐릭터 검색 정상 동작
- [ ] 스킬 탭 렌더링
- [ ] 보석 정보 표시
- [ ] 각성 스킬 구분
- [ ] 원정대 캐릭터 목록

## 자동화 스크립트 (선택사항)

### 함수 추출 스크립트 예시

```bash
#!/bin/bash
# extract-functions.sh

# 사용법: ./extract-functions.sh <컴포넌트 경로> <함수명1> <함수명2> ...
# 예: ./extract-functions.sh CharacterSearch.vue normalizeSkillKey extractGemLabel

COMPONENT=$1
shift
FUNCTIONS=("$@")

for FUNC in "${FUNCTIONS[@]}"; do
  echo "Extracting $FUNC..."

  # 함수 정의 찾기
  START_LINE=$(grep -n "^const $FUNC" "$COMPONENT" | cut -d: -f1)

  # 함수 본문 추출 (간단한 버전 - 실제로는 더 복잡함)
  sed -n "${START_LINE},/^const /p" "$COMPONENT"

  echo "---"
done
```

## 체크리스트

### 추출 전
- [ ] 추출할 함수 목록 작성
- [ ] 함수 의존성 파악 (다른 함수 호출 여부)
- [ ] API 타입 확인
- [ ] 기존 유틸 함수 재사용 가능성 확인

### 추출 중
- [ ] 파일 구조 생성 (`utils/character/` 등)
- [ ] 타입 import 추가
- [ ] JSDoc 작성
- [ ] 함수별 `@example` 추가
- [ ] 상수는 `export const` 로
- [ ] 내부 함수는 export 하지 않음

### 추출 후
- [ ] 원본 컴포넌트에서 import
- [ ] 원본 함수 정의 제거
- [ ] `npm run type-check` 실행
- [ ] `npm run dev` 실행
- [ ] 기능 테스트 수행
- [ ] Git commit (예: `refactor: extract skill utils from CharacterSearch`)

## 트러블슈팅

### 문제: 타입 오류 발생

**증상:**
```
error TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'string'.
```

**해결:**
```typescript
// ❌ Before
export const normalize = (value: string) => { ... }

// ✅ After
export const normalize = (value?: string | null): string => {
  if (!value) return ''
  // ...
}
```

### 문제: Circular dependency

**증상:**
```
Circular dependency detected: CharacterSearch.vue -> utils/skillDataTransform.ts -> CharacterSearch.vue
```

**해결:**
- 타입만 필요하면 `import type { ... }` 사용
- 순환 참조 제거 (함수가 컴포넌트를 참조하지 않도록)

### 문제: 함수 동작 변경됨

**원인:**
- 내부 상태에 의존하던 함수를 순수 함수로 잘못 추출

**해결:**
- 해당 함수는 utils가 아닌 composables로 추출
- 또는 파라미터로 상태를 전달하도록 수정

## 다음 단계

1. **equipmentDataTransform.ts** 작성
   - Equipment stat parsing
   - Transcendence/elixir helpers
   - Quality calculation

2. **arkGridDataTransform.ts** 작성
   - Passive tooltip parsing
   - Roman numeral conversion
   - Ark grid matrix building

3. **cardDataTransform.ts** 작성
   - Card set effect parsing
   - Awakening calculations

4. **모든 utils 완료 후 Phase 2 (Composables 추출)로 진행**

## 참고 자료

- [Plan 파일](../C:/Users/blues/.claude/plans/functional-honking-perlis.md)
- [Vue 3 Composition API](https://vuejs.org/api/composition-api-setup.html)
- [TypeScript Best Practices](https://www.typescriptlang.org/docs/handbook/declaration-files/do-s-and-don-ts.html)
