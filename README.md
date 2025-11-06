# 로스트아크 웹사이트 프로젝트

## 기술 스택
- **Frontend**: Vue.js 3
- **Backend**: Spring Boot 3
- **Database**: MariaDB 11.4
- **Container**: Docker & Docker Compose

## 시작하기

### 1. 환경 변수 설정
`.env` 파일을 열어서 로스트아크 API 키를 입력하세요.
```
LOSTARK_API_KEY=your_api_key_here
```
API 키는 https://developer-lostark.game.onstove.com/ 에서 발급받을 수 있습니다.

### 2. Docker 컨테이너 실행
```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그만 보기
docker-compose logs -f backend
docker-compose logs -f frontend
```

### 3. 접속 주소
- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **Adminer (DB 관리)**: http://localhost:8082
  - 시스템: MySQL
  - 서버: database
  - 사용자명: lostark_user
  - 비밀번호: (.env 파일 참조)
  - 데이터베이스: lostark

### 4. 컨테이너 관리
```bash
# 컨테이너 중지
docker-compose down

# 컨테이너 재시작
docker-compose restart

# 특정 서비스만 재시작
docker-compose restart backend

# 컨테이너와 볼륨까지 모두 삭제 (데이터 초기화)
docker-compose down -v
```

## 프로젝트 구조
```
lostark-project/
├── frontend/              # Vue.js 프로젝트
│   ├── src/
│   ├── package.json
│   └── Dockerfile
├── backend/               # Spring Boot 프로젝트
│   ├── src/
│   ├── build.gradle
│   └── Dockerfile
├── docker-compose.yml     # Docker 설정
├── .env                   # 환경 변수
└── README.md
```

## 개발 가이드

### Frontend 개발
1. `frontend/` 폴더에서 Vue 프로젝트 생성
```bash
cd frontend
npm create vue@latest .
```

2. Vite 설정 (`vite.config.js`)에 Hot Reload 활성화
```javascript
export default defineConfig({
  server: {
    host: '0.0.0.0',
    port: 5173,
    watch: {
      usePolling: true
    }
  }
})
```

### Backend 개발
1. `backend/` 폴더에서 Spring Boot 프로젝트 생성
   - https://start.spring.io/ 에서 생성 후 압축 해제
   - 또는 IDE에서 직접 생성

2. `application.yml` 설정
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

lostark:
  api:
    key: ${LOSTARK_API_KEY}
    base-url: https://developer-lostark.game.onstove.com
```

## Git 사용법
```bash
# 저장소 초기화
git init
git add .
git commit -m "Initial commit"

# GitHub에 푸시
git remote add origin https://github.com/your-username/lostark-project.git
git push -u origin main

# 다른 PC에서
git clone https://github.com/your-username/lostark-project.git
cd lostark-project
# .env 파일 설정 후
docker-compose up -d
```

## 문제 해결

### 포트 충돌
다른 서비스와 포트가 충돌하면 `docker-compose.yml`에서 포트 번호를 변경하세요.

### 컨테이너가 시작되지 않을 때
```bash
# 로그 확인
docker-compose logs backend
docker-compose logs frontend

# 컨테이너 재빌드
docker-compose up -d --build
```

### 데이터베이스 초기화
```bash
docker-compose down -v
docker-compose up -d
```
