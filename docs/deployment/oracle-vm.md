# Oracle VM 배포 가이드 (Docker Compose + GitHub Actions)

이 저장소는 GitHub Actions에서 백엔드 JAR를 빌드한 뒤, Oracle VM으로 전송하고 Docker Compose로 재시작하는 흐름을 지원합니다.

## 1) VM 준비물
- Docker + Docker Compose Plugin 설치
- 배포 디렉토리 준비: `~/lostark`
  - 최초 1회는 레포를 `git clone` 해두는 것을 권장합니다. (디렉토리 구조/`.env` 관리가 쉬움)
  - 최소 요구사항은 아래 경로들이 존재하는 것입니다.
    - `~/lostark/.env`
    - `~/lostark/backend/build/libs/` (Actions가 JAR를 여기에 복사)
    - `~/lostark/docker-compose.prod.yml` (Actions가 같이 복사)

## 2) 동작 방식 요약
- Actions가 `backend`를 빌드해서 `backend/build/libs/*.jar`를 생성
- `appleboy/scp-action`으로 VM의 `~/lostark/backend/build/libs/`로 JAR를 전송
- SSH로 접속해 최신 JAR를 `backend/build/libs/app.jar`로 고정 복사한 뒤, 아래로 백엔드만 재시작
  - `docker compose -f docker-compose.prod.yml up -d --no-build --force-recreate backend`

`docker-compose.prod.yml`에서 `backend` 서비스는 이미지를 빌드하지 않고, `./backend/build/libs/app.jar`를 컨테이너에 마운트해서 실행합니다.

## 3) GitHub Actions 워크플로우
- 파일: `.github/workflows/deploy.yml`
- 주요 포인트:
  - 서버에서 `git pull` 없이도 배포가 진행되도록 JAR + `docker-compose.prod.yml`을 함께 전송
  - 서버에서 `docker compose build`를 하지 않도록 `--no-build` 사용
  - 기존 컨테이너가 떠있는 상태에서도 변경사항이 확실히 반영되도록 `--force-recreate` 사용

## 4) 운영 체크
- 백엔드 헬스 체크: `GET /api/markets/options`
- 컨테이너 확인: `docker compose -f docker-compose.prod.yml ps`

