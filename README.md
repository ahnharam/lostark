# 로스트아크 웹사이트 프로젝트

# 로스트아크 웹사이트 프로젝트

## 스택
- **Frontend**: Vue 3 + Vite
- **Backend**: Spring Boot 3 (Gradle)
- **Database**: MariaDB 11.4
- **컨테이너**: Docker / Docker Compose

## 폴더 구조
```
lostark-project/
├── frontend/              # Vue 앱 (Dockerfile, Dockerfile.dev 포함)
├── backend/               # Spring Boot (Dockerfile, Dockerfile.dev 포함)
├── docker-compose.yml     # 개발용 Compose
├── docker-compose.prod.yml# 운영용 Compose
├── docs/                  # 문서(준비/배포/가이드)
├── .env                   # 로컬 환경 변수
└── README.md
```

## 빠른 시작
1) `.env` 준비 (`.env.example` 참고, DB/LOSTARK_API_KEY 등)  
2) 원클릭 준비: `bash scripts/dev-prepare.sh` (프론트 의존성 설치, DB/백엔드 기동, 헬스 체크)  
3) 프론트: `npm run dev` (또는 `npm run dev:host`, `docker compose up -d frontend`)  
4) 백엔드: `./gradlew bootRun` (또는 `./gradlew --no-daemon bootRun`)  
5) 주요 포트: 프론트 5173, 백엔드 8080, DB 3307, Adminer 8082  
6) 헬스 체크:  
```bash
docker compose ps
curl -i http://localhost:8080/api/markets/options
curl -s http://localhost:8080/api/admin/market-stats/status
```

## 주요 기능/구성
- **경매/거래소 조회**: `frontend/src/components/AuctionMenu.vue`, 백엔드 `market/controller/MarketController`
- **일별 통계 수집**: 스케줄(04:30/수 06:05) + 수동 캡처(`POST /api/admin/market-stats/capture`, 비동기)  
  - 저장: `market_daily_stats`, `market_item_assets`  
  - 프론트 관리자 화면: `frontend/src/components/AdminStats.vue` (검색/페이징/수집 중 오버레이)
- **상태 배지**: 메인 메뉴에서 `/api/markets/options` 헬스 기반 서버 상태 표시

## 문서/가이드
- 단일 진입: `docs/dev-quickstart.md` (준비/헬스/구성/트러블슈팅/문서 목록)  
- 배포: `docs/deployment/` (Oracle VM/Vercel 등)  
- 프론트 UX/기능: `frontend/docs/UX_OVERVIEW.md`, `frontend/docs/CHARACTER_RANKING_GUIDE.md`, `frontend/docs/ARK_GRID_GUIDE.md`  
- 기타: `docs/lostark-armory.md`, `docs/mcp-usage.md`, `docs/documentation-guidelines.md`

## 운영/배포
- 개발: `docker compose up -d --build`
- 운영 시뮬레이션: `docker compose -f docker-compose.prod.yml up -d --build` (backend는 `backend/build/libs/app.jar` 마운트 방식)
- 관리: `docker compose restart`, `docker compose logs -f backend`

## 트러블슈팅 메모
- 포트 충돌 시 Compose 포트 수정.  
- Gradle 캐시 권한 오류: `sudo chown -R "$USER" backend/.gradle`.  
- 프론트 OPTIONS/CORS 실패: `VITE_API_BASE_URL` 확인 + 백엔드(8080) 기동 여부 확인.
