# Dev Quickstart & Structure Guide

로컬 준비/헬스체크/구성 개요를 한 곳에 모았습니다.

## 0. 사전 요구 사항
- Docker & Docker Compose
- Node 20.x 이상
- `.env`에 DB/LOSTARK_API_KEY 등 필수 값 설정 (`.env.example` 참고)

## 1. 원클릭 준비
```bash
bash scripts/dev-prepare.sh
```
- 프론트 의존성 설치(`npm install`), DB/백엔드 컨테이너 기동, 백엔드 헬스 체크까지 수행.

## 2. 서비스/포트
- 백엔드: http://localhost:8080
- DB: localhost:3307 (MariaDB, `.env`의 `DB_USER/DB_PASSWORD`)
- Adminer: http://localhost:8082
- 프론트(Vite): http://localhost:5173

## 3. 빠른 헬스 체크
```bash
docker compose ps                                  # 컨테이너 상태
curl -i http://localhost:8080/api/markets/options  # 백엔드 옵션/헬스
curl -s http://localhost:8080/api/admin/market-stats/status  # 통계 수집 상태(running/scanned/saved/targetDate)
```

## 4. 실행 명령
### 프론트
```bash
cd frontend
npm run dev
# 컨테이너로 실행: docker compose up -d frontend
# 전체 IP 바인딩: npm run dev -- --host 0.0.0.0 --port 5173 (또는 npm run dev:host)
```

### 백엔드
```bash
cd backend
./gradlew bootRun
# 실패 시: ./gradlew --no-daemon bootRun
# Gradle 캐시 권한 오류 시: sudo chown -R "$USER" backend/.gradle
```

## 5. 주요 엔드포인트
| 경로 | 설명 | 비고 |
| --- | --- | --- |
| `GET /api/markets/options` | 거래소 옵션/헬스 체크 | CORS 프리플라이트 용 |
| `GET /api/skill-codes/{characterName}` | 로아 웹 프로필 기반 스킬 코드 조회 | 프로필 HTML 파싱 후 `SkillRecommend` 호출 |
| `GET /api/admin/market-stats/status` | 통계 수집 상태 조회 | `running/scanned/saved/targetDate/startedAt` |
| `POST /api/admin/market-stats/capture` | 일별 통계 수동 수집(비동기) | `date=yyyy-MM-dd` 옵션 |
| `GET /api/admin/market-stats/recent` | 일별 통계 조회 | `page/size/q` (ID 또는 이름 검색) |
| `POST /api/markets/options/sync` | 거래소 카테고리 동기화 | 카테고리 테이블 초기화/갱신 |

## 6. 프론트 구성 요약

### 6.1 프로젝트 아키텍처
- **진입점**: `frontend/src/main.ts` → `App.vue` → `router/index.ts` → `components/MainLayout.vue`
- **라우팅**: Vue Router 중첩 라우트 구조 (2025-01 리팩토링 완료)
  - 루트 레이아웃: `MainLayout.vue` (헤더/메뉴/상태 배지)
  - 자식 라우트: 각 메뉴별 nested routes (딥링킹, 브라우저 히스토리 지원)
  - 레거시 URL 자동 리다이렉트 (`/:menu` → `/:menu/:submenu`)

### 6.2 라우트 구조 (`frontend/src/router/index.ts`)
```
/                          → MainLayout.vue
  ├─ ''                    → CharacterSearch.vue (기본)
  ├─ auction/              → AuctionMenu.vue (wrapper)
  │   ├─ market            → auction/MarketView.vue
  │   └─ auction-house     → auction/AuctionHouseView.vue
  ├─ reforge/              → ReforgeMenu.vue (integrated)
  │   ├─ normal            → ReforgeMenu.vue (submenu: normal)
  │   ├─ advanced          → ReforgeMenu.vue (submenu: advanced)
  │   ├─ blunt-thorn       → reforge/BluntThornCalculator.vue
  │   └─ supersonic        → reforge/SupersonicCalculator.vue
  ├─ raid/                 → RaidMenu.vue (wrapper)
  │   └─ party             → RaidPartyManager.vue
  ├─ admin/                → AdminMenu.vue (wrapper)
  │   ├─ market-records    → AdminStats.vue
  │   └─ raid-catalog      → AdminRaidCatalog.vue
  ├─ friends               → FriendManager.vue
  ├─ characters            → CharacterManager.vue
  └─ life                  → LifeMenu.vue
```

### 6.3 주요 컴포넌트 구조

#### 메뉴 래퍼 (Router-view 사용)
- `MainLayout.vue`: 메인 레이아웃, `<router-view>` 사용
- `AuctionMenu.vue`: 경매장 래퍼 (market, auction-house)
- `RaidMenu.vue`: 레이드 래퍼 (party)
- `AdminMenu.vue`: 관리자 래퍼 (market-records, raid-catalog)

#### 실제 컨텐츠 컴포넌트
- **경매/거래소**:
  - `auction/MarketView.vue`: 거래소 검색/정렬/상세 그래프 (1,500줄)
  - `auction/AuctionHouseView.vue`: 경매장 (준비중)

- **캐릭터/검색**:
  - `CharacterSearch.vue`: 캐릭터 검색 메인 화면
  - Composables: `useCharacterData.ts`, `useEquipmentData.ts`, `useSkillData.ts` 등

- **재련**:
  - `ReforgeMenu.vue`: 일반/상급 제련 계산기 (통합)
  - `reforge/BluntThornCalculator.vue`: 뭉가 계산기
  - `reforge/SupersonicCalculator.vue`: 음돌 계산기

- **레이드**:
  - `RaidPartyManager.vue`: 레이드 멤버 구성/DM 초대

- **관리자**:
  - `AdminStats.vue`: 거래소 일별 통계 (검색/페이징/수동 캡처)
  - `AdminRaidCatalog.vue`: 레이드 카탈로그 관리

#### 공통 컴포넌트 (`components/common/`)
- `ThemeToggle.vue`: 다크모드 토글
- `MyInfoModal.vue`: 사용자 정보 모달
- `LoadingSpinner.vue`: 로딩 스피너
- `LazyImage.vue`: 지연 로딩 이미지
- `CustomSelect.vue`: 커스텀 셀렉트

### 6.4 Composables (비즈니스 로직 분리)
위치: `frontend/src/composables/character/`

| Composable | 역할 | 주요 Export |
|-----------|------|------------|
| `useCharacterData.ts` | 캐릭터 기본 정보 | `characterProfile`, `isExpeditionLeader` |
| `useEquipmentData.ts` | 장비/아이템 관리 | `equipmentItems`, `accessoryItems`, `braceletItem` |
| `useEngravingData.ts` | 각인 정보 | `classEngravingNames`, `activeEngravings` |
| `useGemData.ts` | 보석 정보 | `gemColorMap`, `gemTypeLabels` |
| `useSkillData.ts` | 스킬/보석 슬롯 | `combatSkillCatalog`, `skillGemSlotsBySkill` |

### 6.5 API 레이어
- **API 클라이언트**: `frontend/src/api/lostark.ts`
  - 서버 상태: `checkServerStatus()`
  - 캐릭터: `getCharacterProfile()`, `getCharacterArmory()`
  - 거래소: `searchMarketItems()`, `getMarketCategories()`, `getMarketItemDetail()`
  - 통계: `getMarketDailyStatsRecent()`, `triggerMarketStatsCapture()`, `getMarketStatsStatus()`
  - 스킬: `getSkillCodes()`, `getSkillRecommendations()`

- **타입 정의**: `frontend/src/api/types/`
  - `armory.ts`: 아모리 관련 타입
  - `skills.ts`: 스킬 관련 타입
  - `index.ts`: 거래소/통계 타입

### 6.6 빌드 및 개발 명령어
```bash
# 개발 서버
npm run dev                 # localhost:5173
npm run dev:host            # 0.0.0.0:5173 (외부 접근)

# 타입 체크
npm run type-check          # Vue TypeScript 타입 검증

# 린트/포맷
npm run lint                # ESLint 검사
npm run format              # Prettier 포맷팅

# 빌드
npm run build               # 프로덕션 빌드
npm run preview             # 빌드 결과 프리뷰
```

## 7. 백엔드 구성 요약 (Spring Boot)
- 패키지 루트: `backend/src/main/java/com/lostark/backend`
- 설정:
  - 스케줄: `config/SchedulingConfig`(@EnableScheduling)
  - 보안/세션/CSRF/CORS: `config/security/SecurityConfig`
  - `config/WebConfig`는 현재 비어있습니다.
- 도메인/기능:  
  - 거래소 수집/통계: `market/service/MarketSyncService`  
  - 스케줄러: `market/scheduler/MarketStatsScheduler` (04:30 매일, 수 06:05, 수동 비동기)  
  - API: `market/controller/MarketController`(검색/카테고리/상세), `MarketStatsAdminController`(통계 조회/캡처/상태)  
  - 저장소: `market/entity` (`MarketItemDailyStat`, `MarketItemAsset`, `MarketCategory` 등), `market/repository`
  - 마이그레이션: `market/migration/MarketDailyStatsMigration` (legacy 통계 테이블 → 신규 통계 테이블 자동 이관)
  - 외부연동: `lostark/client/LostArkApiClient` (로아 API)
- 실행: `./gradlew bootRun` (8080) 또는 Docker Compose `backend` 서비스.

## 8. 스케줄/통계 특징
- 아이템 메타(아이템ID/이름/아이콘/카테고리): `market_item_assets`
- 일별 거래소 통계(아이템ID/날짜/지표들): `market_item_daily_stats`
- 레거시: 기존 `market_daily_stats`는 백엔드 기동 시 `MarketDailyStatsMigration`에서 신규 테이블로 자동 이관(검증 후 수동 정리 권장)
- 스케줄: 04:30(전일), 수 06:05(초기화 후). 수동 캡처는 비동기로 처리.
- AdminStats 화면: 검색/페이징, 수집 중 오버레이에 진행상황 표출(저장 10건 단위), 완료 시 자동 갱신.
- 캡처는 카테고리(`market_categories`)를 기준으로 루프를 돌며, 카테고리가 비어있으면 캡처 시작 시 자동으로 동기화 시도.

## 9. 트러블슈팅 체크리스트
1) `.env` 존재/값 확인  
2) `docker compose logs backend database` 확인  
3) 포트 충돌 여부(3307/8080/8082/5173)  
4) 프론트 CORS/OPTIONS 실패: `VITE_API_BASE_URL` 확인 + 백엔드 기동 여부 확인  
5) Vercel + 별도 백엔드(도메인 다름)에서 `POST`가 403(CSRF)인 경우:
   - 먼저 `GET /api/auth/csrf`가 200이고 `Set-Cookie: XSRF-TOKEN=...`를 내려주는지 확인
   - 브라우저의 cross-site 요청에서는 `SameSite=Lax` 쿠키가 `POST`에 포함되지 않을 수 있으므로, 백엔드 `.env`에서 아래를 설정
     - `SESSION_COOKIE_SAME_SITE=None`, `SESSION_COOKIE_SECURE=true`
     - `CSRF_COOKIE_SAME_SITE=None`, `CSRF_COOKIE_SECURE=true`
   - `CORS_ALLOWED_ORIGINS`는 브라우저의 `Origin`과 **완전히 일치**해야 하므로, 보통 끝 `/` 없이 `https://<your-vercel-domain>` 형태로 설정
6) Vercel에서 새로고침/직접 진입 시 404(`NOT_FOUND`)가 나는 경우:
   - Vue Router가 history 모드(`createWebHistory`)라서, 모든 경로를 `index.html`로 rewrite 해야 합니다.
   - `frontend/vercel.json`이 존재하는지 확인하고(Vercel 프로젝트 Root Directory가 `frontend`인지도 확인)
7) 관리자 수동 캡처가 안 도는 경우:  
   - 백엔드 로그에 `[MarketStatsAdmin] manual capture accepted`가 찍히는지 확인  
   - `/api/admin/market-stats/status`에서 `running/scanned/saved`가 증가하는지 확인  
   - 카테고리 비어있으면 `/api/markets/options/sync`로 먼저 동기화(또는 캡처 시작 시 자동 동기화 로그 확인)  
8) 프론트 툴팁: 브라우저 기본 `title` 기반 툴팁 사용 금지. `popup-surface--tooltip`로만 표출하고 `:title`/`title` 속성은 제거한다.  
9) 로그에 가끔 `Broken pipe` / `ClientAbortException` / `AsyncRequestNotUsableException`가 찍히는 경우:  
   - 보통 `LazyImage`가 로드하던 `/api/proxy/image` 요청을 브라우저가 취소(페이지 이동/스크롤/탭 전환 등)하면서 연결이 끊긴 경우입니다.  
   - 서버가 이미지를 내려주던 중 클라이언트가 먼저 끊으면 Tomcat/Spring이 `Broken pipe`를 남길 수 있으며, 기능상 치명적인 오류가 아닙니다.  
   - (참고) 과거에는 에러 응답(`ErrorResponse`)을 `image/png`로 쓰려다 `No converter ... preset Content-Type 'image/png'` 경고가 추가로 발생할 수 있었고, 현재는 해당 케이스를 별도 처리해 로그 노이즈를 줄였습니다.  

## 10. 자주 찾는 위치
- 문서: `docs/dev-quickstart.md`(본 문서)
- 준비 스크립트: `scripts/dev-prepare.sh`
- 환경 변수: `.env.example` / `.env`
- 프론트 상태 배지/메뉴: `frontend/src/components/MainLayout.vue`
- 관리자 통계 화면: `frontend/src/components/AdminStats.vue`
- 통계 스케줄/저장: `backend/src/main/java/.../market/scheduler/`, `.../service/MarketSyncService.java`

## 11. 개발 스킬 및 주요 패턴

### 11.1 컴포넌트 개발 패턴

#### 새 페이지/메뉴 추가하기
1. **컴포넌트 생성**: `frontend/src/components/` 하위에 컴포넌트 생성
2. **라우트 등록**: `router/index.ts`에 라우트 추가
   ```typescript
   {
     path: 'new-menu',
     name: 'new-menu',
     component: () => import('@/components/NewMenu.vue'),
     meta: { menu: 'new-menu' }
   }
   ```
3. **메뉴 추가**: `MainLayout.vue`의 `mainMenuItems` 배열에 추가
   ```typescript
   { key: 'new-menu', label: '새 메뉴', badge: '' }
   ```

#### 서브메뉴가 있는 메뉴 추가하기
1. **래퍼 컴포넌트 생성**: 예) `NewMenuWrapper.vue`
   ```vue
   <template>
     <router-view v-slot="{ Component }">
       <component :is="Component" />
     </router-view>
   </template>
   ```

2. **중첩 라우트 설정**: `router/index.ts`
   ```typescript
   {
     path: 'new-menu',
     name: 'new-menu',
     component: () => import('@/components/NewMenuWrapper.vue'),
     redirect: { name: 'new-menu-sub1' },
     meta: { menu: 'new-menu' },
     children: [
       {
         path: 'sub1',
         name: 'new-menu-sub1',
         component: () => import('@/components/newmenu/Sub1View.vue'),
         meta: { menu: 'new-menu', submenu: 'sub1' }
       }
     ]
   }
   ```

3. **서브메뉴 정의**: `MainLayout.vue`의 `subMenus` 객체에 추가
   ```typescript
   'new-menu': [
     { key: 'sub1', label: '서브메뉴1' },
     { key: 'sub2', label: '서브메뉴2' }
   ]
   ```

### 11.2 Composable 작성 패턴

#### 비즈니스 로직을 Composable로 분리하기
```typescript
// composables/useExample.ts
import { computed, ref } from 'vue'
import type { SomeType } from '@/api/types'

export function useExample(props: { data: SomeType }) {
  // State
  const localState = ref<string>('')

  // Computed
  const derivedValue = computed(() => {
    return someTransformation(props.data)
  })

  // Methods
  const doSomething = () => {
    // business logic
  }

  // Return public API
  return {
    localState,
    derivedValue,
    doSomething
  }
}
```

#### Composable 사용하기
```vue
<script setup lang="ts">
import { useExample } from '@/composables/useExample'

const props = defineProps<{ data: SomeType }>()

const { derivedValue, doSomething } = useExample(props)
</script>
```

### 11.3 API 호출 패턴

#### 새 API 엔드포인트 추가하기
1. **타입 정의**: `frontend/src/api/types/index.ts`
   ```typescript
   export interface NewDataType {
     id: number
     name: string
   }
   ```

2. **API 함수 추가**: `frontend/src/api/lostark.ts`
   ```typescript
   export async function getNewData(): Promise<NewDataType[]> {
     const response = await fetch(`${API_BASE_URL}/api/new-data`)
     if (!response.ok) throw new Error('Failed to fetch')
     return response.json()
   }
   ```

3. **컴포넌트에서 사용**:
   ```vue
   <script setup lang="ts">
   import { ref, onMounted } from 'vue'
   import { lostarkApi } from '@/api/lostark'

   const data = ref<NewDataType[]>([])
   const loading = ref(false)

   onMounted(async () => {
     loading.value = true
     try {
       data.value = await lostarkApi.getNewData()
     } catch (error) {
       console.error(error)
     } finally {
       loading.value = false
     }
   })
   </script>
   ```

### 11.4 스타일링 패턴

#### CSS 변수 사용 (다크모드 대응)
```css
.my-component {
  /* 색상은 항상 CSS 변수 사용 */
  background: var(--card-bg, #ffffff);
  color: var(--text-primary, #111827);
  border: 1px solid var(--border-color, #e5e7eb);
}
```

#### 주요 CSS 변수 목록
- `--card-bg`: 카드 배경색
- `--bg-secondary`: 보조 배경색
- `--text-primary`: 주요 텍스트 색상
- `--text-secondary`: 보조 텍스트 색상
- `--text-muted`: 희미한 텍스트 색상
- `--border-color`: 테두리 색상
- `--primary-color`: 기본 강조 색상
- `--shadow-sm`, `--shadow-lg`: 그림자

### 11.5 타입 안전성 보장

#### Props 타입 정의
```vue
<script setup lang="ts">
// Generic 타입 사용
interface Props {
  items: string[]
  count?: number  // optional
}

const props = defineProps<Props>()

// 또는 withDefaults 사용
const props = withDefaults(defineProps<Props>(), {
  count: 0
})
</script>
```

#### Emits 타입 정의
```vue
<script setup lang="ts">
// Typed emits
const emit = defineEmits<{
  update: [value: string]
  delete: [id: number]
}>()

// 사용
emit('update', 'new value')
emit('delete', 123)
</script>
```

### 11.6 성능 최적화 팁

#### KeepAlive 사용
```vue
<!-- 탭 전환 시 상태 유지 -->
<router-view v-slot="{ Component }">
  <KeepAlive>
    <component :is="Component" />
  </KeepAlive>
</router-view>
```

#### Lazy Loading
```typescript
// 라우트에서 컴포넌트 지연 로딩
component: () => import('@/components/HeavyComponent.vue')
```

#### Computed vs Watch
```typescript
// ✅ Good: Computed 사용 (선언적, 캐싱)
const fullName = computed(() => `${firstName.value} ${lastName.value}`)

// ❌ Bad: Watch로 동일 기능 구현
watch([firstName, lastName], () => {
  fullName.value = `${firstName.value} ${lastName.value}`
})
```

### 11.7 자주 사용하는 Git 워크플로우

#### 기능 개발 워크플로우
```bash
# 1. 최신 코드 pull
git pull origin master

# 2. 기능 브랜치 생성 (선택사항)
git checkout -b feature/new-feature

# 3. 개발 및 테스트
npm run type-check
npm run lint

# 4. 커밋
git add .
git commit -m "Add new feature

🤖 Generated with Claude Code
Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"

# 5. Push
git push origin feature/new-feature
```

#### 빠른 핫픽스
```bash
git add .
git commit -m "Fix critical bug in market search"
git push origin master
```

### 11.8 디버깅 팁

#### Vue DevTools 활용
- 브라우저 확장 프로그램 설치
- 컴포넌트 트리 검사
- Pinia 스토어 상태 확인
- 라우터 히스토리 추적

#### console.log 대신 개발자 도구 사용
```typescript
// ❌ Bad
console.log('data:', data)

// ✅ Good: Debugger 사용
debugger  // 브라우저에서 자동 중단점
```

#### 타입 에러 해결
```bash
# 타입 체크로 에러 확인
npm run type-check

# 캐시 문제 시
rm -rf node_modules/.vite
npm run dev
```

### 11.9 코드 리뷰 체크리스트

개발 완료 후 다음 항목들을 확인하세요:

- [ ] `npm run type-check` 통과
- [ ] `npm run lint` 통과 (또는 자동 수정됨)
- [ ] 불필요한 console.log 제거
- [ ] Props/Emits 타입 정의 완료
- [ ] CSS 변수 사용 (하드코딩된 색상 없음)
- [ ] 에러 처리 구현 (try-catch, loading state)
- [ ] 반응형 대응 (모바일/태블릿)
- [ ] 다크모드에서 정상 작동 확인
- [ ] 브라우저 뒤로가기 정상 작동 (라우팅)

## 12. 주요 변경 이력

### 2025-01: Phase 4 라우팅 구조 개선 (완료)
**목표**: URL 기반 네비게이션, 딥링킹, 브라우저 히스토리 지원

**주요 변경사항**:
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
- ✅ 딥링킹 지원 (`/auction/market`, `/reforge/blunt-thorn` 등)
- ✅ 브라우저 뒤로/앞으로 버튼 정상 작동
- ✅ URL 공유 가능 (북마크, 외부 링크)
- ✅ 레거시 URL 호환성 (쿼리 파라미터 보존)
- ✅ 타입 안전성 보장 (type-check 통과)

**관련 문서**:
- `docs/phase4-routing-guide.md`: 상세 구현 가이드
- `docs/phase2-3-completion-report.md`: Phase 2-3 완료 보고서
- `docs/refactoring-code-snippets.md`: 재사용 가능한 코드 스니펫

### 2024-12: Phase 2-3 컴포넌트 리팩토링 (완료)
**목표**: CharacterSearch.vue 크기 축소 및 재사용성 향상

**주요 변경사항**:
1. **Composables 추출** (총 5개, 1,781+ 줄 분리)
   - `useCharacterData.ts`: 캐릭터 기본 정보
   - `useEquipmentData.ts`: 장비/아이템 관리
   - `useEngravingData.ts`: 각인 정보
   - `useGemData.ts`: 보석 정보
   - `useSkillData.ts`: 스킬/보석 슬롯

2. **UI 컴포넌트 추출** (총 4개)
   - `EquipmentGrid.vue`: 장비 그리드 레이아웃
   - `StatPanel.vue`: 스탯 패널
   - `SkillPanel.vue`: 스킬 패널
   - `GemPanel.vue`: 보석 패널

**성과**:
- ✅ CharacterSearch.vue 크기 ~50% 감소
- ✅ 비즈니스 로직 재사용 가능
- ✅ 타입 안전성 향상
- ✅ 유지보수성 개선

## 13. 추가 참고 문서 목록
- 루트
  - `README.md`: 전체 프로젝트 개요
  - `COMPONENT_UPDATE_SUMMARY.md`: 컴포넌트 변경 요약
- docs/
  - `dev-quickstart.md`: **본 문서** (개발 빠른 시작 가이드)
  - `documentation-guidelines.md`: 문서 작성 규칙
  - `mcp-usage.md`: MCP 사용 안내
  - `lostark-armory.md`: 로아 아모리 API 참고
  - `phase4-routing-guide.md`: Phase 4 라우팅 가이드 ⭐
  - `phase2-3-completion-report.md`: Phase 2-3 완료 보고서
  - `refactoring-code-snippets.md`: 코드 스니펫 모음
  - `deployment/`: 배포/인프라 가이드
    - `README.md`, `oracle-vm.md`, `vercel.md`, `railway.md`, `freedb.md`
- frontend/
  - `frontend/README.md`: 프론트 전용 안내
  - `frontend/docs/UX_OVERVIEW.md`: UX 방향/컨셉
  - `frontend/docs/CHARACTER_RANKING_GUIDE.md`: 캐릭터 랭킹 화면 가이드
  - `frontend/docs/ARK_GRID_GUIDE.md`: 아크 그리드 관련 설명

---

**📌 이 문서 하나로 프로젝트 전체를 빠르게 파악할 수 있습니다!**
- 환경 설정, 실행 방법, API 엔드포인트
- 프론트/백엔드 구조 및 주요 컴포넌트
- 개발 패턴, 스킬, 트러블슈팅
- 최신 변경 이력 및 참고 문서
