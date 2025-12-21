# 리팩토링 코드 스니펫 & 자동화 패턴

이 문서는 Phase 4 및 향후 리팩토링 작업에서 재사용할 수 있는 코드 스니펫과 패턴을 정리합니다.

---

## 📋 목차

1. [Composable 생성 템플릿](#composable-생성-템플릿)
2. [UI 컴포넌트 추출 템플릿](#ui-컴포넌트-추출-템플릿)
3. [라우팅 패턴](#라우팅-패턴)
4. [타입 정의 패턴](#타입-정의-패턴)
5. [테스트 체크리스트](#테스트-체크리스트)

---

## Composable 생성 템플릿

### 기본 Composable 구조

```typescript
/**
 * [기능명] 데이터 관리 Composable
 *
 * [원본 컴포넌트]에서 추출한 [기능] 관련 비즈니스 로직입니다.
 * [주요 역할 설명]을 담당합니다.
 */

import { computed, type Ref } from 'vue'
import type { /* 필요한 타입 */ } from '@/api/types/[타입 경로]'
import { /* 필요한 유틸리티 */ } from '@/utils/[유틸리티 경로]'

// ============================================================================
// Types
// ============================================================================

export interface [ExportedType] {
  // 타입 정의
}

// ============================================================================
// Helper Functions (Internal)
// ============================================================================

/**
 * [헬퍼 함수 설명]
 */
const helperFunction = (param: Type): ReturnType => {
  // 구현
}

// ============================================================================
// Composable
// ============================================================================

export const use[FunctionName] = (
  param1: Ref<Type1>,
  param2: Ref<Type2>
) => {
  /**
   * [Computed 설명]
   */
  const computedValue = computed(() => {
    // 구현
  })

  return {
    computedValue
  }
}
```

### 사용 예시

```typescript
// CharacterSearch.vue에서

import { use[FunctionName] } from '@/composables/[path]/use[FunctionName]'

const { computedValue } = use[FunctionName](
  param1,
  param2
)
```

---

## UI 컴포넌트 추출 템플릿

### 기본 컴포넌트 구조

```vue
<template>
  <div class="component-name">
    <!-- 컴포넌트 내용 -->
  </div>
</template>

<script setup lang="ts">
import { /* 필요한 imports */ } from 'vue'
import type { /* 필요한 타입 */ } from '@/api/types/[타입 경로]'

/**
 * [타입 설명]
 */
export interface [ExportedInterface] {
  // 타입 정의
}

const props = defineProps<{
  /**
   * [Prop 설명]
   */
  propName: Type
}>()

const emit = defineEmits<{
  /**
   * [Emit 설명]
   */
  eventName: [paramType]
}>()

// 컴포넌트 로직
</script>

<style scoped>
/* BEM 스타일 유지 */
.component-name {
  /* 스타일 */
}
</style>
```

### Props Down, Events Up 패턴

```typescript
// 부모 컴포넌트에서
<ChildComponent
  :data="computedData"
  @action="handleAction"
/>

// 자식 컴포넌트
const props = defineProps<{
  data: DataType
}>()

const emit = defineEmits<{
  action: [payload: ActionPayload]
}>()

const handleClick = () => {
  emit('action', { /* payload */ })
}
```

---

## 라우팅 패턴

### 중첩 라우트 구조

```typescript
// src/router/index.ts

import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/components/MainLayout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/components/Home.vue')
        },
        {
          path: 'feature',
          name: 'feature',
          redirect: { name: 'feature-default' },
          children: [
            {
              path: 'sub1',
              name: 'feature-sub1',
              component: () => import('@/components/feature/Sub1.vue')
            },
            {
              path: 'sub2',
              name: 'feature-sub2',
              component: () => import('@/components/feature/Sub2.vue')
            }
          ]
        }
      ]
    }
  ]
})

export default router
```

### 레거시 URL 리다이렉트

```typescript
// 기존 URL 패턴을 새 구조로 리다이렉트
{
  path: ':legacyParam(old-pattern)',
  redirect: to => {
    const param = to.params.legacyParam as string
    const defaultSubMenus: Record<string, string> = {
      'old1': 'new1',
      'old2': 'new2'
    }
    return {
      path: `/new-path/${defaultSubMenus[param]}`,
      query: to.query  // 쿼리 파라미터 보존
    }
  }
}
```

### MainLayout에서 router-view 사용

```vue
<template>
  <div class="main-layout">
    <nav class="main-nav">
      <router-link :to="{ name: 'home' }">홈</router-link>
      <router-link :to="{ name: 'feature-sub1' }">기능 1</router-link>
    </nav>

    <main class="layout-content">
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

// 현재 활성 메뉴 추출
const activeMenu = computed(() => route.meta.menu as string || 'default')
</script>
```

---

## 타입 정의 패턴

### API 응답 타입

```typescript
// src/api/types/[feature].ts

export interface FeatureResponse {
  status: string
  data: FeatureData[]
}

export interface FeatureData {
  id: string
  name: string
  value: number
}
```

### Composable 반환 타입

```typescript
// Composable 내부
export interface ComposableReturn {
  data: ComputedRef<DataType>
  loading: Ref<boolean>
  error: Ref<Error | null>
}

export const useFeature = (): ComposableReturn => {
  // 구현
}
```

### 컴포넌트 Props 타입

```typescript
// Component.vue
export interface ComponentProps {
  /**
   * [설명]
   */
  id: string

  /**
   * [설명]
   * @default false
   */
  disabled?: boolean
}

const props = withDefaults(defineProps<ComponentProps>(), {
  disabled: false
})
```

---

## 테스트 체크리스트

### Phase별 검증 체크리스트

#### Phase 2: Composables 추출
```bash
# 1. 타입 체크
npm run type-check
# ✅ 에러 0개 확인

# 2. 빌드 테스트
npm run build-only
# ✅ 빌드 성공 확인

# 3. 개발 서버 실행
npm run dev
# ✅ 컴파일 에러 없음 확인

# 4. 수동 기능 테스트
# - [ ] 모든 Computed 값 정상 표시
# - [ ] 데이터 변환 로직 정상 동작
# - [ ] 에러 핸들링 정상 동작
```

#### Phase 3: UI 컴포넌트 추출
```bash
# 1. 타입 체크
npm run type-check

# 2. 시각적 회귀 테스트
# - [ ] 레이아웃 변경 없음
# - [ ] 스타일 깨짐 없음
# - [ ] 애니메이션 정상 동작

# 3. 상호작용 테스트
# - [ ] Props 전달 정상
# - [ ] Events emit 정상
# - [ ] v-model 양방향 바인딩 정상
```

#### Phase 4: 라우팅 개선
```bash
# 1. 타입 체크
npm run type-check

# 2. 라우트 네비게이션 테스트
# - [ ] 직접 URL 접근
# - [ ] router-link 클릭
# - [ ] 프로그래매틱 네비게이션
# - [ ] 브라우저 뒤로/앞으로 버튼

# 3. 레거시 URL 리다이렉트 테스트
# - [ ] 기존 URL → 새 URL 리다이렉트
# - [ ] 쿼리 파라미터 보존
# - [ ] Hash 파라미터 보존 (필요시)
```

---

## 자동화 프롬프트 템플릿

### Composable 생성 프롬프트

```
다음 조건으로 Composable을 생성해주세요:

**파일 위치**: `src/composables/[category]/use[FunctionName].ts`

**추출 대상**: [원본 파일] 파일의 다음 Computed 속성들:
- `computedProperty1`
- `computedProperty2`
- `computedProperty3`

**의존성**:
- 입력: [Ref<Type1>], [Ref<Type2>]
- 필요한 헬퍼 함수: [helper1], [helper2]

**요구사항**:
1. JSDoc 주석 포함
2. 타입 안전성 확보
3. 헬퍼 함수는 composable 내부에 포함
4. Export할 인터페이스는 상단에 정의

**테스트 항목**:
- [ ] 타입 체크 통과
- [ ] 원본 파일에서 import 및 사용
- [ ] 기존 기능 정상 동작
```

### UI 컴포넌트 추출 프롬프트

```
다음 조건으로 UI 컴포넌트를 추출해주세요:

**파일 위치**: `src/components/[category]/[ComponentName].vue`

**추출 대상**: [원본 파일]의 다음 템플릿 섹션:
- [템플릿 라인 범위]

**Props 정의**:
- `prop1`: [Type] - [설명]
- `prop2`: [Type] - [설명]

**Emits 정의**:
- `event1`: [paramType] - [설명]
- `event2`: [paramType] - [설명]

**요구사항**:
1. BEM 스타일 클래스 유지
2. Props down, Events up 패턴 준수
3. TypeScript 타입 명시
4. JSDoc 주석 포함

**테스트 항목**:
- [ ] 타입 체크 통과
- [ ] 시각적 회귀 없음
- [ ] Props 전달 정상
- [ ] Events emit 정상
```

### 라우팅 개선 프롬프트

```
다음 조건으로 라우팅을 개선해주세요:

**새 라우트 구조**:
```
/
├── /feature
│   ├── /sub1
│   ├── /sub2
│   └── /sub3
```

**레거시 URL 매핑**:
- `/old-feature` → `/feature/sub1`
- `/old-feature?param=value` → `/feature/sub1?param=value` (쿼리 보존)

**요구사항**:
1. MainLayout에서 router-view 사용
2. 중첩 라우트 설계
3. 레거시 URL 리다이렉트
4. 쿼리 파라미터 보존
5. meta 필드 활용 (activeMenu 등)

**테스트 항목**:
- [ ] 직접 URL 접근
- [ ] router-link 네비게이션
- [ ] 브라우저 히스토리 정상 동작
- [ ] 레거시 URL 리다이렉트 정상
```

---

## 반복 작업 자동화

### Git Commit 패턴

```bash
# Phase 2: Composable 추출
git add src/composables/[category]/use[FunctionName].ts
git add src/components/[OriginalComponent].vue
git commit -m "refactor: extract use[FunctionName] composable

- Extract [computed1], [computed2] from [OriginalComponent]
- Move helper functions to composable
- Add TypeScript types
- Pass type-check

Related to Phase 2.X"

# Phase 3: UI 컴포넌트 추출
git add src/components/[category]/[NewComponent].vue
git add src/components/[OriginalComponent].vue
git commit -m "refactor: extract [NewComponent] component

- Extract [template section] from [OriginalComponent]
- Define Props and Emits
- Preserve BEM styles
- No visual regression

Related to Phase 3"

# Phase 4: 라우팅 개선
git add src/router/index.ts
git add src/components/MainLayout.vue
git commit -m "refactor: improve routing structure

- Add nested routes for [feature]
- Implement legacy URL redirects
- Update MainLayout to use router-view
- Preserve query parameters

Related to Phase 4"
```

### 파일 생성 스크립트 (Bash)

```bash
#!/bin/bash
# create-composable.sh

COMPOSABLE_NAME=$1
CATEGORY=$2

if [ -z "$COMPOSABLE_NAME" ] || [ -z "$CATEGORY" ]; then
  echo "Usage: ./create-composable.sh [ComposableName] [category]"
  exit 1
fi

FILE_PATH="src/composables/${CATEGORY}/use${COMPOSABLE_NAME}.ts"

cat > "$FILE_PATH" << 'EOF'
/**
 * [기능명] 데이터 관리 Composable
 *
 * [원본 컴포넌트]에서 추출한 [기능] 관련 비즈니스 로직입니다.
 * [주요 역할 설명]을 담당합니다.
 */

import { computed, type Ref } from 'vue'

// ============================================================================
// Types
// ============================================================================

// ============================================================================
// Helper Functions (Internal)
// ============================================================================

// ============================================================================
// Composable
// ============================================================================

export const use${COMPOSABLE_NAME} = () => {
  return {

  }
}
EOF

echo "✅ Created: $FILE_PATH"
```

### 사용법

```bash
chmod +x create-composable.sh
./create-composable.sh SkillData character
# ✅ Created: src/composables/character/useSkillData.ts
```

---

## Phase 4 준비: AuctionMenu 분리 패턴

### 1단계: 분석

```typescript
// AuctionMenu.vue 현재 구조 분석
// - 라인 수: 2,315줄
// - 서브메뉴: Market, AuctionHouse
// - 공통 로직: 필터, 검색, 페이지네이션
// - 독립 로직: 각 메뉴별 데이터 처리
```

### 2단계: Composable 추출 (필요시)

```typescript
// src/composables/auction/useMarketData.ts
export const useMarketData = (
  filters: Ref<MarketFilters>,
  page: Ref<number>
) => {
  const items = computed(() => { /* ... */ })
  const total = computed(() => { /* ... */ })

  return { items, total }
}

// src/composables/auction/useAuctionData.ts
export const useAuctionData = (
  filters: Ref<AuctionFilters>,
  page: Ref<number>
) => {
  const bids = computed(() => { /* ... */ })
  const total = computed(() => { /* ... */ })

  return { bids, total }
}
```

### 3단계: View 컴포넌트 분리

```vue
<!-- src/components/auction/MarketView.vue -->
<template>
  <div class="market-view">
    <!-- Market 전용 UI -->
  </div>
</template>

<script setup lang="ts">
import { useMarketData } from '@/composables/auction/useMarketData'

const { items, total } = useMarketData(filters, page)
</script>

<!-- src/components/auction/AuctionHouseView.vue -->
<template>
  <div class="auction-house-view">
    <!-- AuctionHouse 전용 UI -->
  </div>
</template>

<script setup lang="ts">
import { useAuctionData } from '@/composables/auction/useAuctionData'

const { bids, total } = useAuctionData(filters, page)
</script>
```

### 4단계: 라우팅 업데이트

```typescript
// src/router/index.ts
{
  path: 'auction',
  name: 'auction',
  redirect: { name: 'auction-market' },
  children: [
    {
      path: 'market',
      name: 'auction-market',
      component: () => import('@/components/auction/MarketView.vue')
    },
    {
      path: 'auction-house',
      name: 'auction-house',
      component: () => import('@/components/auction/AuctionHouseView.vue')
    }
  ]
},
// 레거시 리다이렉트
{
  path: ':menu(auction)',
  redirect: to => ({
    path: '/auction/market',
    query: to.query
  })
}
```

---

## 문제 해결 패턴

### 타입 에러: Module has no exported member

**문제**:
```typescript
import type { SomeType } from '@/api/types/wrong-path'
// Error: Module has no exported member 'SomeType'
```

**해결**:
```bash
# 1. 타입이 실제로 어디에 정의되어 있는지 검색
grep -r "export.*SomeType" src/api/types/

# 2. 올바른 경로에서 import
import type { SomeType } from '@/api/types/correct-path'
```

### 순환 참조 문제

**문제**:
```typescript
// A.ts
import { B } from './B'

// B.ts
import { A } from './A'
// Error: Circular dependency
```

**해결**:
```typescript
// types.ts (공통 타입 파일)
export interface SharedType { /* ... */ }

// A.ts
import type { SharedType } from './types'

// B.ts
import type { SharedType } from './types'
```

### Computed 의존성 누락

**문제**:
```typescript
const result = computed(() => {
  // someRef 변경해도 재계산 안됨
  const value = someRef.value
  return processData(value)
})
```

**해결**:
```typescript
const result = computed(() => {
  // .value 접근을 computed 내부에서
  return processData(someRef.value)
})
```

---

## 베스트 프랙티스

### 1. Composable 설계
- ✅ 단일 책임 원칙 (하나의 composable은 하나의 기능)
- ✅ 명확한 입출력 타입 정의
- ✅ 헬퍼 함수는 내부에 캡슐화
- ✅ JSDoc 주석으로 사용법 명시

### 2. 컴포넌트 설계
- ✅ Props down, Events up 패턴
- ✅ BEM 스타일 유지
- ✅ 과도한 중첩 피하기 (최대 3단계)
- ✅ Scoped styles 사용

### 3. 라우팅 설계
- ✅ RESTful URL 구조
- ✅ 중첩 라우트로 계층 표현
- ✅ 레거시 URL 리다이렉트 보존
- ✅ 쿼리 파라미터 보존

### 4. 타입 안전성
- ✅ any 타입 금지
- ✅ 명시적 타입 선언
- ✅ 인터페이스 > 타입 별칭
- ✅ Generic 적극 활용

### 5. Git 관리
- ✅ Phase별 별도 커밋
- ✅ 의미 있는 커밋 메시지
- ✅ 커밋 전 타입 체크
- ✅ 기능 단위 커밋

---

**작성일**: 2025-01-XX
**버전**: 1.0
**관련 문서**: phase2-3-completion-report.md, phase4-routing-guide.md
