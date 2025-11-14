# Character Ranking Feature Guide

캐릭터 정보 메뉴에 랭킹 섹션을 추가하기 위한 API 조사, 설계 지침, 기본 구조 초안을 정리했습니다. 본 문서는 Lost Ark Open API의 리더보드 데이터를 활용해 프론트엔드/백엔드를 동시에 확장하는 것을 목표로 합니다.

---

## 1. 목표와 범위
- `CharacterSearch` 화면 안에서 **"랭킹" 탭**을 제공하고, 현재 캐릭터가 속해 있는 시즌/모드의 순위를 상단에 하이라이트한다.
- PvP 경쟁전, 증명의 전장 모드들처럼 Lost Ark Open API에서 제공하는 **리더보드 데이터를 조회**해 최상위 목록과 필터를 노출한다.
- 캐릭터 검색 → 랭킹 탭 진입 시 백엔드가 Lost Ark API를 호출하고, 프론트는 캐시/로딩/빈 상태를 명확하게 처리한다.

### 포함 범위
1. Lost Ark API 조사 및 파라미터 가이드.
2. 백엔드 확장 (API 클라이언트, 서비스, REST 엔드포인트, 캐시 전략).
3. 프론트엔드 UI/UX 설계 (탭 구성, 상태 관리, 컴포넌트 구조).
4. 향후 기능 확장 포인트 (필터, 즐겨찾기 서버/클래스 기준 랭킹 등).

---

## 2. Lost Ark Leaderboard API 조사

| 항목 | 값/설명 |
|------|---------|
| Base URL | `https://developer-lostark.game.onstove.com` (기존 `.env`의 `LOSTARK_API_BASE_URL` 재사용) |
| Endpoint | `GET /leaderboards/rankings` |
| Header | `Authorization: bearer {LOSTARK_API_KEY}` (기존 클라이언트에 동일) |

### 2.1 Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `leaderboardCode` | string | ✅ | 리더보드 식별코드. 예) `0101`(경쟁전), `0201`(증명의 전장: 팀 데스매치), `0202`(대장전) 등. 최신 코드는 [공식 개발자 문서](https://developer-lostark.game.onstove.com/guide/leaderboards) 참고. |
| `seasonId` | string | ⛔ (선택) | 특정 시즌을 명시적으로 조회. 미전달 시 현재 진행 중인 시즌으로 처리. |
| `page` | number | ⛔ | 1기반 페이지 번호. 기본 1, 최대 100. |

> 참고: 단일 호출에 최대 200개의 랭킹 항목이 내려옵니다. 특정 캐릭터가 상위 200위 밖에 있다면 추가 페이지 호출이 필요합니다.

### 2.2 Response Shape (요약)

```json
[
  {
    "Rank": 1,
    "CharacterName": "테스트",
    "ServerName": "니나브",
    "CharacterClassName": "버서커",
    "ItemAvgLevel": "1620.83",
    "GuildName": "하이퍼리언",
    "Rating": 2140,
    "Division": 3,
    "Tier": 10,
    "SeasonName": "경쟁전 시즌 4",
    "UpdatedDate": "2024-02-02T15:00:00Z"
  }
]
```

필드는 리더보드 유형에 따라 약간씩 달라지나, 공통적으로 `Rank`, `CharacterName`, `ServerName`, `CharacterClassName`, `ItemAvgLevel`, `Rating`, `GuildName`, `UpdatedDate`가 포함됩니다. 증명의 전장 계열은 승/패, 점수(`Score`), 길드 대신 팀 정보가 내려올 수 있으므로 DTO 설계 시 선택 필드(optional)을 고려합니다.

### 2.3 제약 및 권고
- **Rate limit**: 기본 10req/s (계정별). 캐릭터 검색마다 1~3회 호출을 예상하고, `@Cacheable` 혹은 사용자 레벨 캐시를 적용합니다.
- **Timeout**: 공용 API의 P95가 1~2초 수준이므로 백엔드에서 타임아웃(5s)과 재시도는 짧게 설정합니다.
- **필수 값 검증**: 존재하지 않는 `leaderboardCode`를 전달하면 404가 반환되므로 컨트롤러 단에서 화이트리스트 필터링을 권장합니다.

---

## 3. 데이터 모델 & DTO

### 3.1 백엔드 DTO (Java)
```java
public record LeaderboardEntryDto(
    int rank,
    String characterName,
    String serverName,
    String characterClassName,
    String itemAvgLevel,
    Integer rating,
    Integer tier,
    Integer division,
    Integer winCount,
    Integer loseCount,
    String guildName,
    Instant updatedDate
) {}
```

- 선택 필드는 `Integer`/`String` nullable 처리.
- `itemAvgLevel`은 API가 문자열을 반환하므로 parsing helper를 별도로 둬서 Double로 변환 가능.

### 3.2 응답 포맷
```json
{
  "summary": {
    "leaderboardCode": "0101",
    "seasonId": "S4",
    "page": 1,
    "totalFetched": 200,
    "lastUpdated": "2024-02-02T15:00:00Z",
    "highlightedCharacter": {
      "rank": 135,
      "distanceFromTop": 134
    }
  },
  "entries": [ /* LeaderboardEntryDto[] */ ]
}
```

`highlightedCharacter`는 요청한 캐릭터가 결과 내에 존재할 때만 포함. 존재하지 않으면 `null`을 내려 프론트가 별도 안내 문구를 띄우도록 합니다.

---

## 4. 백엔드 구조 설계

### 4.1 신규 컴포넌트
| 레이어 | 파일/클래스 | 책임 |
|--------|-------------|------|
| Client | `LeaderboardApiClient` | `LostArkApiClient` 유사 구조. `/leaderboards/rankings` 호출 메서드 제공. |
| Domain Service | `LeaderboardDomainService` | 다중 페이지 조회, 캐릭터 하이라이트 탐색, 호출 실패 시 폴백 처리. |
| Application Service | `RankingService` | 컨트롤러 진입점. 캐시 전략, 파라미터 검증. |
| Controller | `RankingController` (`/api/rankings`) | 프론트 요청 매핑. ex) `GET /api/rankings?leaderboardCode=0101&characterName=...`. |

### 4.2 캐시 전략
- `leaderboardCode+seasonId+page` 조합으로 최대 5분 캐시 (ConcurrentHashMap or Caffeine).
- 캐릭터 하이라이트 탐색 시 최대 3페이지(600위)까지 순차 호출 후 미확인 시 `notFoundInRanking` 플래그 세팅.
- `LostArk` API 장애 시 1분 동안 폴백 메시지를 캐시에 저장해 반복 호출 차단.

### 4.3 Validation 흐름
1. 컨트롤러에서 `leaderboardCode` 화이트리스트 (`List.of("0101","0201","0202","0203")`) 검증.
2. `characterName`이 없으면 일반 리더보드(탑랭킹) 조회, 있으면 하이라이트 모드.
3. 서비스에서 캐시 조회 → 미스 시 Lost Ark API 호출.
4. Error 발생 시 `ApiException`으로 래핑해 `GlobalExceptionHandler` 재사용.

---

## 5. 프론트엔드 UI/UX 설계

### 5.1 네비게이션 구조
- `CharacterSearch.vue`의 결과 카드 내 탭에 `랭킹` 탭을 추가.
- 탭 진입과 동시에 `useRankingStore` 혹은 `useRanking` composable이 API 호출을 트리거.

### 5.2 상태 관리
```ts
type RankingState = {
  entries: LeaderboardEntry[];
  summary: RankingSummary | null;
  loading: boolean;
  error: ApiError | null;
  filters: {
    leaderboardCode: string; // 기본 '0101'
    seasonId?: string;
    page: number;
  };
}
```

- Vue `watch`를 이용해 `filters` 변화를 감지하고 자동 fetch.
- `cachedResults` 맵을 만들어 동일 필터 반복 호출 시 캐시된 데이터를 즉시 표시.

### 5.3 컴포넌트 구조
| 컴포넌트 | 역할 |
|----------|------|
| `RankingTab.vue` | 탭 컨테이너. 필터, 요약, 리스트, Empty/Error 상태를 orchestration. |
| `RankingFilterBar.vue` | 드롭다운(리더보드 종류), 시즌 선택, 페이지 이동(Prev/Next) 컨트롤. |
| `RankingHighlightCard.vue` | 현재 캐릭터 순위/증감/서버/클래스 메타 정보 표시. |
| `RankingTable.vue` | 순위 리스트 테이블. 스켈레톤과 무한 스크롤/페이지 모드 옵션. |
| `RankingEmptyState.vue` | 해당 조건에서 랭킹 없음/데이터 비공개 안내. |

### 5.4 UI States
- **Loading**: 탭 진입 시 상단에 Progress bar + 리스트 skeleton row 5개 노출.
- **Character Not Ranked**: highlight가 없지만 상위 랭킹 리스트는 존재하는 경우 → "현재 캐릭터는 600위 밖에 위치해 있어요" 메시지.
- **API Error**: 백엔드 에러 메시지를 `ErrorMessage` 컴포넌트로 연결.
- **No Data**: 리더보드 자체에 데이터가 없으면 EmptyState 사용.

### 5.5 인터랙션
1. 캐릭터 검색 성공 → `activeTab === 'ranking'` 시 `loadRanking(characterName, defaultCode)`.
2. 필터 변경 → Debounce(300ms) 후 fetch.
3. 테이블 row 클릭 → `CharacterDetailModal` 재활용 없이 간단 툴팁/Popover로 서버/길드 안내.

---

## 6. API 통신 흐름

```
CharacterSearch.vue
  └── RankingTab.vue
        └── useRanking(characterName, filters)
              └── frontend lostarkApi.getRanking(filters)
                    └── backend GET /api/rankings
                          └── RankingService
                                └── LeaderboardDomainService
                                      └── LostArk API
```

- 프론트 `lostarkApi`에 `getRanking(params)` 메서드를 추가 (`GET /rankings`).
- 백엔드 컨트롤러는 `@RequestParam characterName`, `leaderboardCode`, `seasonId`, `page`를 수신.

---

## 7. 기본 API 계약 (Frontend ↔ Backend)

```
GET /api/rankings?leaderboardCode=0101&characterName=캐릭터명&page=1
Response 200:
{
  "summary": {
    "leaderboardCode": "0101",
    "seasonId": "S4",
    "page": 1,
    "highlightedCharacter": {
      "characterName": "화이트버서커",
      "rank": 132,
      "rating": 1980,
      "itemAvgLevel": "1610.00"
    },
    "lastUpdated": "2024-02-02T15:00:00Z"
  },
  "entries": [ ... LeaderboardEntry ... ]
}
```

에러 형식은 기존 API(`ErrorMessage.vue` 기준)와 동일하게 유지합니다.

---

## 8. 구현 체크리스트
1. [ ] `LeaderboardApiClient` + `/leaderboards/rankings` 호출 메서드 추가.
2. [ ] `RankingService`/`RankingController` 추가 및 캐시 설정.
3. [ ] `lostarkApi.getRanking` 프론트 API 헬퍼 구현.
4. [ ] `useRanking` 컴포저블 및 상태/캐시 구현.
5. [ ] `RankingTab` 및 하위 컴포넌트 마크업 작성, 기존 탭 네비게이션에 연결.
6. [ ] Skeleton/Empty/Error/Highlight UI 완성 및 스냅샷 테스트.
7. [ ] 통합 테스트: 캐릭터 검색 → 랭킹 탭 전환 → 필터/페이지 조작.

---

## 9. 향후 확장 아이디어
- 랭킹 데이터 스케줄러: 매 30분마다 백엔드가 선호 리더보드 캐시를 미리 채워 API 호출 지연을 최소화.
- 서버/클래스 필터: 프론트에서 로컬 필터링하여 동일 리더보드 내에서 사용자 관심 항목만 보여주기.
- 그래프 뷰: 최근 7일간 캐릭터 순위 변화를 Sparkline으로 시각화 (추가 엔드포인트 필요).
- 공유 링크: 랭킹 상태를 캡처해 SNS 공유 이미지로 생성.

---

## 10. 랭킹 세분화 가이드

경쟁전(공식 리더보드) 외에도 자체 데이터베이스 기반 “내 위치” 요약을 서브 카테고리로 노출합니다. `RankingTab` 상단에 pill 네비게이션을 두고, `경쟁전`은 기존 Lost Ark API 결과를, 나머지 카테고리는 저장된 캐릭터 데이터를 활용한 요약 카드(`RankingMetricCard`)를 표시합니다.

| 카테고리 | 데이터 소스 | 설명 |
|----------|-------------|------|
| 경쟁전 | `/leaderboards/rankings` | 기존 PvP/증명의 전장 리더보드. 필터/페이지 UI 유지. |
| 전체 서버 · 아이템 | DB `characters.itemAvgLevel` | 전체 서버 캐릭터 대비 아이템 레벨 순위. |
| 전체 서버 · 직업 | DB `itemAvgLevel` + `characterClassName` | 동일 직업군 내 글로벌 순위. |
| 내 서버 · 전체 | DB `serverName` + `itemAvgLevel` | 동일 서버 모든 직업과 비교. |
| 내 서버 · 직업 | DB `serverName` + `characterClassName` | 서버 + 직업 조합 별 세부 순위. |
| 원정대 | DB `expeditionLevel` | 원정대 레벨 기준 상대 위치. |
| 수집품 | DB `collectionScore` (수집품 합산) | `Collectibles` API 포인트 합계 기준 순위. |

### 백엔드
- `ProfileRankingService`는 `CharacterRepository.findAll()` 결과를 기준으로 각 카테고리별 순위를 계산합니다.
- `CharacterService`는 캐릭터 조회 시 `collectibles` API를 함께 호출하여 `collectionScore`를 갱신합니다.
- `GET /api/rankings/profile/{characterName}` 응답은 `RankingMetricDto` 묶음을 반환해 프론트가 단일 호출로 모든 요약을 가져올 수 있습니다.

### 프론트엔드
- `useProfileRanking` 컴포저블이 `/rankings/profile/{characterName}`를 캐시하며, `RankingMetricCard`는 순위/총인원/현재 값/퍼센타일을 공통 스타일로 보여줍니다.
- `RankingTab`은 `activeCategory` 상태를 관리하고 `pvp` 이외에는 metric 카드만 출력하여 UI 복잡도를 줄입니다.
- 데이터 없음/에러/로딩 상태를 카드 내부에서 처리하여 카테고리 변경 시 자연스럽게 스켈레톤 → 데이터 전환이 이뤄집니다.

향후에는 동일 구조를 확장해 `원정대 내 다른 캐릭터 비교`, `즐겨찾기 캐릭터 vs 내 캐릭터` 등의 비교 카드도 추가할 수 있습니다.

---

이 문서를 기반으로 이후 작업(백엔드 구현, 프론트 컴포넌트 제작, QA 시나리오)이 단계적으로 진행될 수 있도록, 변경 사항은 `docs/` 폴더에 버전 히스토리를 남겨주세요.
