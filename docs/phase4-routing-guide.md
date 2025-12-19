# Phase 4: 라우팅 구조 개선 가이드

## 📋 개요

**목표**: 단일 라우트(`/:menu?`)를 중첩 라우트 구조로 개선하여 딥링크, 서브메뉴 히스토리, 더 나은 URL 구조 지원

**소요 시간**: 3-4일 (보수적 일정: 5일)
**위험도**: 높음 (레거시 URL 호환성 유지 필요)

---

## 🎯 목표 라우트 구조

### Before (현재)
```
/ 또는 /:menu?
```
- 단일 라우트
- 동적 컴포넌트 교체
- 딥링크 불가
- 서브메뉴 히스토리 미지원

### After (목표)
```
/                           → CharacterSearch
/auction
  /market                   → MarketView
  /auction-house            → AuctionHouseView
/reforge
  /normal                   → ReforgeNormal
  /advanced                 → ReforgeAdvanced
  /blunt-thorn              → ReforgeBluntThorn
  /supersonic               → ReforgeSuperSonic
/raid
  /party                    → RaidParty
  /todo                     → RaidTodo
/admin
  /market-records           → MarketRecords
  /auction-records          → AuctionRecords
```

---

## 📂 파일 구조 변경

### 현재 구조
```
src/components/
├── MainLayout.vue          (동적 컴포넌트 사용)
├── CharacterSearch.vue
├── AuctionMenu.vue         (2,315줄 - Market + AuctionHouse)
├── ReforgeMenu.vue         (4개 서브메뉴)
├── RaidMenu.vue
└── AdminMenu.vue
```

### 목표 구조
```
src/
├── router/
│   └── index.ts            ⚠️ 수정 필요
├── components/
│   ├── MainLayout.vue      ⚠️ 수정 필요
│   ├── CharacterSearch.vue (유지)
│   ├── auction/
│   │   ├── AuctionMenu.vue    (Wrapper - router-view만)
│   │   ├── MarketView.vue     ⭐ 신규 (800줄)
│   │   └── AuctionHouseView.vue ⭐ 신규 (800줄)
│   ├── reforge/
│   │   ├── ReforgeMenu.vue    (Wrapper)
│   │   ├── NormalView.vue     ⭐ 신규
│   │   ├── AdvancedView.vue   ⭐ 신규
│   │   ├── BluntThornView.vue ⭐ 신규
│   │   └── SuperSonicView.vue ⭐ 신규
│   ├── raid/
│   │   ├── RaidMenu.vue       (Wrapper)
│   │   ├── PartyView.vue      ⭐ 신규
│   │   └── TodoView.vue       ⭐ 신규
│   └── admin/
│       ├── AdminMenu.vue      (Wrapper)
│       ├── MarketRecordsView.vue ⭐ 신규
│       └── AuctionRecordsView.vue ⭐ 신규
└── composables/
    └── auction/               ⭐ 신규 (필요시)
        ├── useMarketData.ts
        └── useAuctionData.ts
```

---

## 🔧 Step-by-Step 작업 순서

### Step 1: 라우터 구조 설계 (1일)

#### 1.1 새 라우트 정의

**파일**: `src/router/index.ts`

```typescript
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: () => import('@/components/MainLayout.vue'),
      children: [
        // ========================================
        // 캐릭터 검색 (홈)
        // ========================================
        {
          path: '',
          name: 'character-search',
          component: () => import('@/components/CharacterSearch.vue'),
          meta: { menu: 'character-search' }
        },

        // ========================================
        // 경매장 (Auction)
        // ========================================
        {
          path: 'auction',
          name: 'auction',
          component: () => import('@/components/auction/AuctionMenu.vue'),
          redirect: { name: 'auction-market' },
          meta: { menu: 'auction' },
          children: [
            {
              path: 'market',
              name: 'auction-market',
              component: () => import('@/components/auction/MarketView.vue'),
              meta: { menu: 'auction', submenu: 'market' }
            },
            {
              path: 'auction-house',
              name: 'auction-house',
              component: () => import('@/components/auction/AuctionHouseView.vue'),
              meta: { menu: 'auction', submenu: 'auction-house' }
            }
          ]
        },

        // ========================================
        // 재련 (Reforge)
        // ========================================
        {
          path: 'reforge',
          name: 'reforge',
          component: () => import('@/components/reforge/ReforgeMenu.vue'),
          redirect: { name: 'reforge-normal' },
          meta: { menu: 'reforge' },
          children: [
            {
              path: 'normal',
              name: 'reforge-normal',
              component: () => import('@/components/reforge/NormalView.vue'),
              meta: { menu: 'reforge', submenu: 'normal' }
            },
            {
              path: 'advanced',
              name: 'reforge-advanced',
              component: () => import('@/components/reforge/AdvancedView.vue'),
              meta: { menu: 'reforge', submenu: 'advanced' }
            },
            {
              path: 'blunt-thorn',
              name: 'reforge-blunt-thorn',
              component: () => import('@/components/reforge/BluntThornView.vue'),
              meta: { menu: 'reforge', submenu: 'blunt-thorn' }
            },
            {
              path: 'supersonic',
              name: 'reforge-supersonic',
              component: () => import('@/components/reforge/SuperSonicView.vue'),
              meta: { menu: 'reforge', submenu: 'supersonic' }
            }
          ]
        },

        // ========================================
        // 레이드 (Raid)
        // ========================================
        {
          path: 'raid',
          name: 'raid',
          component: () => import('@/components/raid/RaidMenu.vue'),
          redirect: { name: 'raid-party' },
          meta: { menu: 'raid' },
          children: [
            {
              path: 'party',
              name: 'raid-party',
              component: () => import('@/components/raid/PartyView.vue'),
              meta: { menu: 'raid', submenu: 'party' }
            },
            {
              path: 'todo',
              name: 'raid-todo',
              component: () => import('@/components/raid/TodoView.vue'),
              meta: { menu: 'raid', submenu: 'todo' }
            }
          ]
        },

        // ========================================
        // 관리자 (Admin)
        // ========================================
        {
          path: 'admin',
          name: 'admin',
          component: () => import('@/components/admin/AdminMenu.vue'),
          redirect: { name: 'admin-market-records' },
          meta: { menu: 'admin' },
          children: [
            {
              path: 'market-records',
              name: 'admin-market-records',
              component: () => import('@/components/admin/MarketRecordsView.vue'),
              meta: { menu: 'admin', submenu: 'market-records' }
            },
            {
              path: 'auction-records',
              name: 'admin-auction-records',
              component: () => import('@/components/admin/AuctionRecordsView.vue'),
              meta: { menu: 'admin', submenu: 'auction-records' }
            }
          ]
        },

        // ========================================
        // 레거시 URL 리다이렉트
        // ========================================
        {
          path: ':menu(auction|reforge|raid|admin)',
          redirect: to => {
            const menu = to.params.menu as string
            const defaultSubMenus: Record<string, string> = {
              auction: 'market',
              reforge: 'normal',
              raid: 'party',
              admin: 'market-records'
            }
            return {
              path: `/${menu}/${defaultSubMenus[menu]}`,
              query: to.query  // 쿼리 파라미터 보존
            }
          }
        }
      ]
    }
  ]
})

export default router
```

---

### Step 2: MainLayout 업데이트 (0.5일)

#### 2.1 동적 컴포넌트 → router-view

**파일**: `src/components/MainLayout.vue`

**Before**:
```vue
<template>
  <main class="layout-content">
    <component :is="currentComponent" />
  </main>
</template>

<script setup lang="ts">
const componentMap = {
  'character-search': CharacterSearch,
  'auction': AuctionMenu,
  'reforge': ReforgeMenu,
  // ...
}

const currentComponent = computed(() => {
  return componentMap[activeMenu.value] || CharacterSearch
})
</script>
```

**After**:
```vue
<template>
  <div class="main-layout">
    <header class="main-header">
      <nav class="main-nav">
        <router-link
          :to="{ name: 'character-search' }"
          :class="{ active: activeMenu === 'character-search' }"
        >
          캐릭터 검색
        </router-link>
        <router-link
          :to="{ name: 'auction-market' }"
          :class="{ active: activeMenu === 'auction' }"
        >
          경매장
        </router-link>
        <router-link
          :to="{ name: 'reforge-normal' }"
          :class="{ active: activeMenu === 'reforge' }"
        >
          재련
        </router-link>
        <!-- 기타 메뉴 -->
      </nav>
    </header>

    <main class="layout-content">
      <router-view v-slot="{ Component }">
        <component :is="Component" />
      </router-view>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// route.meta에서 activeMenu 추출
const activeMenu = computed(() => route.meta.menu as string || 'character-search')
</script>
```

#### 2.2 서브메뉴도 router-link로 전환

```vue
<!-- 각 Menu 컴포넌트에서 -->
<template>
  <div class="auction-menu">
    <nav class="submenu-nav">
      <router-link
        :to="{ name: 'auction-market' }"
        :class="{ active: activeSubmenu === 'market' }"
      >
        거래소
      </router-link>
      <router-link
        :to="{ name: 'auction-house' }"
        :class="{ active: activeSubmenu === 'auction-house' }"
      >
        경매장
      </router-link>
    </nav>

    <!-- 서브 라우트 렌더링 -->
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeSubmenu = computed(() => route.meta.submenu as string || '')
</script>
```

---

### Step 3: AuctionMenu.vue 분리 (2일)

#### 3.1 현재 AuctionMenu.vue 분석

```vue
<!-- AuctionMenu.vue (2,315줄) -->
<template>
  <div class="auction-menu">
    <!-- 공통 헤더/서브메뉴 네비게이션 -->
    <div v-if="activeSubmenu === 'market'">
      <!-- Market 전용 UI (약 800줄) -->
    </div>
    <div v-else-if="activeSubmenu === 'auction-house'">
      <!-- AuctionHouse 전용 UI (약 800줄) -->
    </div>
  </div>
</template>

<script setup lang="ts">
// 공통 로직 (약 300줄)
// Market 로직 (약 400줄)
// AuctionHouse 로직 (약 500줄)
</script>
```

#### 3.2 Composable 추출 (선택사항)

**파일**: `src/composables/auction/useMarketData.ts`

```typescript
/**
 * 거래소 데이터 관리 Composable
 */

import { computed, ref, type Ref } from 'vue'
import type { MarketItem, MarketFilters } from '@/api/types/auction'

export const useMarketData = (
  filters: Ref<MarketFilters>,
  page: Ref<number>
) => {
  const items = ref<MarketItem[]>([])
  const total = ref(0)
  const loading = ref(false)

  const filteredItems = computed(() => {
    // 필터링 로직
    return items.value.filter(/* ... */)
  })

  const fetchItems = async () => {
    loading.value = true
    try {
      // API 호출
    } finally {
      loading.value = false
    }
  }

  return {
    items: filteredItems,
    total,
    loading,
    fetchItems
  }
}
```

**파일**: `src/composables/auction/useAuctionData.ts`

```typescript
/**
 * 경매장 데이터 관리 Composable
 */

import { computed, ref, type Ref } from 'vue'
import type { AuctionBid, AuctionFilters } from '@/api/types/auction'

export const useAuctionData = (
  filters: Ref<AuctionFilters>,
  page: Ref<number>
) => {
  const bids = ref<AuctionBid[]>([])
  const total = ref(0)
  const loading = ref(false)

  const filteredBids = computed(() => {
    // 필터링 로직
    return bids.value.filter(/* ... */)
  })

  const fetchBids = async () => {
    loading.value = true
    try {
      // API 호출
    } finally {
      loading.value = false
    }
  }

  return {
    bids: filteredBids,
    total,
    loading,
    fetchBids
  }
}
```

#### 3.3 View 컴포넌트 생성

**파일**: `src/components/auction/MarketView.vue`

```vue
<template>
  <div class="market-view">
    <!-- Market 전용 UI -->
    <div class="market-filters">
      <!-- 필터 UI -->
    </div>

    <div class="market-items">
      <div v-for="item in items" :key="item.id" class="market-item">
        <!-- 아이템 카드 -->
      </div>
    </div>

    <div class="market-pagination">
      <!-- 페이지네이션 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMarketData } from '@/composables/auction/useMarketData'

const filters = ref({
  category: '',
  minPrice: 0,
  maxPrice: 0
})
const page = ref(1)

const { items, total, loading, fetchItems } = useMarketData(filters, page)

// Market 전용 로직
</script>

<style scoped>
/* Market 전용 스타일 (기존 BEM 유지) */
.market-view {
  /* ... */
}
</style>
```

**파일**: `src/components/auction/AuctionHouseView.vue`

```vue
<template>
  <div class="auction-house-view">
    <!-- AuctionHouse 전용 UI -->
    <div class="auction-filters">
      <!-- 필터 UI -->
    </div>

    <div class="auction-bids">
      <div v-for="bid in bids" :key="bid.id" class="auction-bid">
        <!-- 입찰 카드 -->
      </div>
    </div>

    <div class="auction-pagination">
      <!-- 페이지네이션 -->
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuctionData } from '@/composables/auction/useAuctionData'

const filters = ref({
  category: '',
  minBid: 0,
  maxBid: 0
})
const page = ref(1)

const { bids, total, loading, fetchBids } = useAuctionData(filters, page)

// AuctionHouse 전용 로직
</script>

<style scoped>
/* AuctionHouse 전용 스타일 */
.auction-house-view {
  /* ... */
}
</style>
```

#### 3.4 Wrapper 업데이트

**파일**: `src/components/auction/AuctionMenu.vue`

```vue
<template>
  <div class="auction-menu">
    <nav class="auction-submenu">
      <router-link
        :to="{ name: 'auction-market' }"
        :class="{ active: activeSubmenu === 'market' }"
      >
        거래소
      </router-link>
      <router-link
        :to="{ name: 'auction-house' }"
        :class="{ active: activeSubmenu === 'auction-house' }"
      >
        경매장
      </router-link>
    </nav>

    <!-- 공통 컨테이너 -->
    <div class="auction-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeSubmenu = computed(() => route.meta.submenu as string || 'market')
</script>

<style scoped>
/* 공통 스타일만 유지 */
.auction-menu {
  /* ... */
}

.auction-submenu {
  /* ... */
}
</style>
```

---

### Step 4: 동일 패턴으로 나머지 메뉴 분리 (1일)

- ReforgeMenu → 4개 View
- RaidMenu → 2개 View
- AdminMenu → 2개 View

**자동화 프롬프트**:
```
AuctionMenu와 동일한 패턴으로 [MenuName]을 분리해주세요:

1. Composable 추출 (필요시):
   - src/composables/[menu]/use[Feature]Data.ts

2. View 컴포넌트 생성:
   - src/components/[menu]/[Submenu1]View.vue
   - src/components/[menu]/[Submenu2]View.vue

3. Wrapper 업데이트:
   - src/components/[menu]/[MenuName].vue
   - 서브메뉴 네비게이션을 router-link로 변경
   - <router-view /> 추가

4. BEM 스타일 유지
5. 타입 체크 통과
```

---

### Step 5: 레거시 URL 테스트 (0.5일)

#### 5.1 테스트 케이스

```typescript
// tests/routing.spec.ts (수동 테스트)

describe('Legacy URL Redirects', () => {
  test('/auction → /auction/market', async () => {
    await router.push('/auction')
    expect(router.currentRoute.value.name).toBe('auction-market')
  })

  test('/auction?category=weapon → /auction/market?category=weapon', async () => {
    await router.push('/auction?category=weapon')
    expect(router.currentRoute.value.name).toBe('auction-market')
    expect(router.currentRoute.value.query.category).toBe('weapon')
  })

  test('/reforge → /reforge/normal', async () => {
    await router.push('/reforge')
    expect(router.currentRoute.value.name).toBe('reforge-normal')
  })

  // 기타 테스트...
})
```

#### 5.2 수동 테스트 체크리스트

**레거시 URL 접근**:
- [ ] `/auction` → `/auction/market` 리다이렉트
- [ ] `/auction?category=weapon` → `/auction/market?category=weapon` (쿼리 보존)
- [ ] `/reforge` → `/reforge/normal`
- [ ] `/raid` → `/raid/party`
- [ ] `/admin` → `/admin/market-records`

**새 URL 직접 접근**:
- [ ] `/auction/market` 정상 표시
- [ ] `/auction/auction-house` 정상 표시
- [ ] `/reforge/blunt-thorn` 정상 표시
- [ ] `/raid/todo` 정상 표시

**브라우저 네비게이션**:
- [ ] 뒤로 버튼 정상 동작
- [ ] 앞으로 버튼 정상 동작
- [ ] 새로고침 (F5) 후 현재 페이지 유지

**router-link 네비게이션**:
- [ ] 메인 메뉴 클릭
- [ ] 서브메뉴 클릭
- [ ] active 클래스 적용 확인

---

## 🚨 위험 관리

### 높은 위험: 레거시 URL 깨짐

**위험**:
- 사용자 북마크 깨짐
- 외부 링크 깨짐
- SEO 영향

**완화 전략**:
1. **포괄적 리다이렉트**:
   ```typescript
   {
     path: ':menu(auction|reforge|raid|admin)',
     redirect: to => ({
       path: `/${to.params.menu}/${defaultSubMenus[to.params.menu]}`,
       query: to.query
     })
   }
   ```

2. **쿼리 파라미터 보존**:
   ```typescript
   redirect: to => ({
     path: '/new-path',
     query: to.query  // ✅ 쿼리 보존
   })
   ```

3. **Hash 파라미터 보존** (필요시):
   ```typescript
   redirect: to => ({
     path: '/new-path',
     query: to.query,
     hash: to.hash  // ✅ Hash 보존
   })
   ```

4. **Feature Flag** (선택사항):
   ```typescript
   // .env.development
   VITE_USE_NEW_ROUTING=true

   // .env.production
   VITE_USE_NEW_ROUTING=false  // 테스트 완료 후 true
   ```

---

### 중간 위험: 상태 관리 문제

**위험**:
- 라우트 변경 시 상태 초기화
- 서브메뉴 간 이동 시 데이터 손실

**완화 전략**:
1. **Keep-Alive 사용**:
   ```vue
   <router-view v-slot="{ Component }">
     <keep-alive>
       <component :is="Component" />
     </keep-alive>
   </router-view>
   ```

2. **Pinia 사용** (필요시):
   ```typescript
   // src/stores/auction.ts
   export const useAuctionStore = defineStore('auction', () => {
     const filters = ref<MarketFilters>({})
     const page = ref(1)

     return { filters, page }
   })
   ```

---

## ✅ 검증 체크리스트

### Phase 4.1: 라우터 구조 설계
- [ ] 타입 체크 통과
- [ ] 빌드 성공
- [ ] 모든 라우트 정의 완료
- [ ] Meta 필드 설정 완료

### Phase 4.2: MainLayout 업데이트
- [ ] router-view로 교체 완료
- [ ] 메인 메뉴 router-link 적용
- [ ] active 클래스 동작 확인
- [ ] 시각적 회귀 없음

### Phase 4.3: AuctionMenu 분리
- [ ] MarketView.vue 생성 완료
- [ ] AuctionHouseView.vue 생성 완료
- [ ] Composable 추출 (선택)
- [ ] Wrapper 업데이트 완료
- [ ] 기능 정상 동작

### Phase 4.4: 나머지 메뉴 분리
- [ ] ReforgeMenu 분리 완료
- [ ] RaidMenu 분리 완료
- [ ] AdminMenu 분리 완료

### Phase 4.5: 레거시 URL 테스트
- [ ] 모든 레거시 URL 리다이렉트 정상
- [ ] 쿼리 파라미터 보존 확인
- [ ] 브라우저 네비게이션 정상
- [ ] 직접 URL 접근 정상

---

## 📊 성과 예상

| 항목 | Before | After | 개선 |
|------|--------|-------|------|
| 라우트 개수 | 1 | 15+ | **딥링크 지원** |
| AuctionMenu.vue | 2,315줄 | ~400줄 | **-1,915줄** |
| URL 구조 | `/:menu?` | RESTful | **가독성 ↑** |
| 히스토리 지원 | ❌ | ✅ | **UX ↑** |

---

## 🔄 롤백 전략

### Git 커밋 단계별

```bash
# Phase 4.1: 라우터 설계
git add src/router/index.ts
git commit -m "refactor(phase4.1): add nested routing structure"

# Phase 4.2: MainLayout 업데이트
git add src/components/MainLayout.vue
git commit -m "refactor(phase4.2): update MainLayout to use router-view"

# Phase 4.3: AuctionMenu 분리
git add src/components/auction/*
git add src/composables/auction/*
git commit -m "refactor(phase4.3): split AuctionMenu into views"

# Phase 4.4: 나머지 메뉴 분리
git add src/components/reforge/*
git add src/components/raid/*
git add src/components/admin/*
git commit -m "refactor(phase4.4): split remaining menus"

# Phase 4.5: 테스트 완료
git commit -m "refactor(phase4.5): verify legacy URL redirects"
```

### 롤백 시

```bash
# 특정 Phase로 돌아가기
git log --oneline
git revert <commit-hash>

# 또는 hard reset (푸시 전)
git reset --hard <commit-hash>
```

---

## 📝 다음 단계

Phase 4 완료 후:
1. **Phase 5**: Pinia 상태 관리 도입 (선택사항)
2. **Phase 6**: 최종 정리 및 최적화
3. **성능 모니터링**: Core Web Vitals, 번들 크기

---

**작성일**: 2025-01-XX
**버전**: 1.0
**관련 문서**:
- phase2-3-completion-report.md
- refactoring-code-snippets.md
- ../plans/functional-honking-perlis.md
