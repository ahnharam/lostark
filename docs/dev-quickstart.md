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
curl -s http://localhost:8080/api/admin/market-stats/status  # 통계 수집 상태
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
| `GET /api/admin/market-stats/status` | 통계 수집 실행 중 여부 | 관리자 |
| `POST /api/admin/market-stats/capture` | 일별 통계 수동 수집(비동기) | `date=yyyy-MM-dd` 옵션 |
| `GET /api/admin/market-stats/recent` | 일별 통계 조회 | `page/size/q` (ID 또는 이름 검색) |

## 6. 프론트 구성 요약
- 진입: `frontend/src/main.ts` → `App.vue` → `components/MainLayout.vue` (메뉴/컨텐츠 스위치).
- 주요 화면:  
  - 경매: `components/AuctionMenu.vue` (검색/정렬/상세 그래프)  
  - 관리자: `components/AdminStats.vue` (통계 검색/페이징/수동 캡처/수집 중 오버레이)  
  - 검색: `CharacterSearch.vue`, 기타 `ReforgeMenu.vue` / 공통은 `components/common/`
- 라우팅: `frontend/src/router/index.ts` (`/:menu?` 단일 라우트).
- API 래퍼: `frontend/src/api/lostark.ts` + 타입 `frontend/src/api/types/`  
  - 서버 상태: `checkServerStatus()`  
  - 통계: `getMarketDailyStatsRecent`, `triggerMarketStatsCapture`, `getMarketStatsStatus`.
- 빌드/테스트: `npm run dev`, `npm run build`, `npm run lint`.

## 7. 백엔드 구성 요약 (Spring Boot)
- 패키지 루트: `backend/src/main/java/com/lostark/backend`
- 설정: `config/WebConfig`(CORS), `config/SchedulingConfig`(@EnableScheduling)
- 도메인/기능:  
  - 거래소 수집/통계: `market/service/MarketSyncService`  
  - 스케줄러: `market/scheduler/MarketStatsScheduler` (04:30 매일, 수 06:05, 수동 비동기)  
  - API: `market/controller/MarketController`(검색/카테고리/상세), `MarketStatsAdminController`(통계 조회/캡처/상태)  
  - 저장소: `market/entity` (`MarketDailyStat`, `MarketItemAsset`, `MarketCategory` 등), `market/repository`  
  - 외부연동: `lostark/client/LostArkApiClient` (로아 API)
- 실행: `./gradlew bootRun` (8080) 또는 Docker Compose `backend` 서비스.

## 8. 스케줄/통계 특징
- 일별 거래소 통계: `market_daily_stats`, 아이템 메타: `market_item_assets`
- 스케줄: 04:30(전일), 수 06:05(초기화 후). 수동 캡처는 비동기로 처리.
- AdminStats 화면: 검색/페이징, 수집 중 오버레이, 완료 시 자동 갱신.

## 9. 트러블슈팅 체크리스트
1) `.env` 존재/값 확인  
2) `docker compose logs backend database` 확인  
3) 포트 충돌 여부(3307/8080/8082/5173)  
4) 프론트 CORS/OPTIONS 실패: `VITE_API_BASE_URL` 확인 + 백엔드 기동 여부 확인  

## 10. 자주 찾는 위치
- 문서: `docs/dev-quickstart.md`(본 문서)
- 준비 스크립트: `scripts/dev-prepare.sh`
- 환경 변수: `.env.example` / `.env`
- 프론트 상태 배지/메뉴: `frontend/src/components/MainLayout.vue`
- 관리자 통계 화면: `frontend/src/components/AdminStats.vue`
- 통계 스케줄/저장: `backend/src/main/java/.../market/scheduler/`, `.../service/MarketSyncService.java`

## 11. 추가 참고 문서 목록
- 루트
  - `README.md`: 전체 프로젝트 개요.
  - `COMPONENT_UPDATE_SUMMARY.md`: 컴포넌트 변경 요약.
- docs/
  - `documentation-guidelines.md`: 문서 작성 규칙.
  - `mcp-usage.md`: MCP 사용 안내.
  - `lostark-armory.md`: 로아 아모리 API 참고.
  - `deployment/README.md`, `deployment/railway.md`, `deployment/vercel.md`, `deployment/freedb.md`: 배포/인프라 가이드.
- frontend/
  - `frontend/README.md`: 프론트 전용 안내.
  - `frontend/docs/UX_OVERVIEW.md`: UX 방향/컨셉.
  - `frontend/docs/CHARACTER_RANKING_GUIDE.md`: 캐릭터 랭킹 화면 가이드.
  - `frontend/docs/ARK_GRID_GUIDE.md`: 아크 그리드 관련 설명.
