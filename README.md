# 로스트아크 웹사이트 프로젝트

## 개요
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
├── docs/deployment/       # Vercel/Railway/FreeDB 가이드
├── .env                   # 로컬 환경 변수
└── README.md
```

## 로컬 개발 순서
1. **환경 변수 준비**
   - `.env`를 생성하고 다음 값을 채웁니다.
     ```
     LOSTARK_API_KEY=your_api_key_here   # https://developer-lostark.game.onstove.com
     VITE_API_BASE_URL=http://localhost:8080/api
     ```
   - DB 접속 정보는 로컬 Compose 기본값을 사용하거나 필요 시 수정하세요.

2. **컨테이너 실행**
   ```bash
   # 개발 모드 (Vite dev + bootRun)
   docker compose up -d --build

   # 운영 모드 시뮬레이션 (정적 빌드 + JAR)
   docker compose -f docker-compose.prod.yml up -d --build

   # 로그
   docker compose logs -f backend
   docker compose logs -f frontend
   ```

3. **주요 접속 주소**
   - Frontend(Dev): http://localhost:5173
   - Frontend(Nginx 빌드): http://localhost:8081
   - Backend API: http://localhost:8080
   - Adminer: http://localhost:8082 (system: MySQL / server: database / user 비밀번호는 `.env` 참고)

4. **컨테이너 관리**
   ```bash
   docker compose restart              # 전체 재시작
   docker compose restart backend      # 서비스별 재시작
   docker compose down                 # 중지
   docker compose down -v              # 볼륨 포함 초기화
   ```

## 개발 가이드
- **Frontend**
  - `frontend/`에서 Vite dev 서버를 실행하면 실시간 HMR이 적용됩니다.
  - `vite.config.ts`의 `server.host` / `watch.usePolling` 설정은 Docker 개발환경에서도 안정적으로 동작하도록 구성했습니다.
  - `npm run build`로 배포 산출물(`dist`)을 생성합니다.

- **Backend**
  - `backend/`는 Gradle 기반이며 `./gradlew bootRun`으로 실행합니다.
  - `src/main/resources/application.yml`은 `SPRING_DATASOURCE_*`, `LOSTARK_API_KEY` 등 환경 변수를 주입받도록 설계되어 다른 인프라로 옮겨도 설정만 교체하면 됩니다.

## Git 사용 순서
```bash
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/your-username/lostark-project.git
git push -u origin main

# 다른 PC에서
git clone https://github.com/your-username/lostark-project.git
cd lostark-project
cp .env.example .env && # 값 입력
docker compose up -d
```

## 문제 해결
- **포트 충돌**: `docker-compose*.yml`에서 포트를 조정합니다.
- **컨테이너 시작 실패**  
  ```bash
  docker compose logs backend
  docker compose logs frontend
  docker compose up -d --build
  ```
- **DB 초기화**  
  ```bash
  docker compose down -v
  docker compose up -d
  ```

## 클라우드/호스팅 배포
- Frontend: **Vercel** (정적 빌드, `VITE_API_BASE_URL`을 Railway 도메인으로 설정)
- Backend: **Railway** (Dockerfile 또는 JAR 실행, FreeDB 연결)
- Database: **FreeDB** (Hosted MariaDB)

플랫폼별 세부 절차와 환경 변수 매트릭스는 `docs/deployment` 폴더를 참고하세요.
