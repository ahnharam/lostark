#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo "[prep] workspace: $ROOT"

if [ ! -f "$ROOT/.env" ]; then
  echo "[prep] .env 가 없습니다. .env.example을 복사해 DB/LOSTARK_API_KEY 등을 채워주세요."
  exit 1
fi

echo "[prep] 프론트 의존성 확인"
if [ ! -d "$ROOT/frontend/node_modules" ]; then
  (cd "$ROOT/frontend" && npm install)
else
  echo "[prep] frontend/node_modules 존재 → 건너뜀"
fi

echo "[prep] docker compose로 DB/백엔드 기동"
(cd "$ROOT" && docker compose up -d database backend)

wait_for() {
  local url=$1
  local timeout=${2:-120}
  local start
  start=$(date +%s)
  while true; do
    if curl -fsS "$url" >/dev/null 2>&1; then
      return 0
    fi
    sleep 2
    now=$(date +%s)
    if [ $((now - start)) -ge "$timeout" ]; then
      echo "[prep] 타임아웃: $url"
      return 1
    fi
  done
}

echo "[prep] 백엔드 헬스 체크 (options)"
wait_for "http://localhost:8080/api/markets/options" 180 || true

echo "[prep] Adminer: http://localhost:8082  / DB 포트: 3307  / 백엔드: http://localhost:8080"
echo "[prep] 프론트 개발 서버: npm run dev (또는 docker compose up -d frontend)"
