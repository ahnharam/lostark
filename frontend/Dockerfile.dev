FROM node:20-alpine

WORKDIR /app

# package.json만 먼저 복사 (캐시 활용)
COPY package*.json ./

# 의존성 설치 (package.json이 변경되지 않으면 캐시 사용)
RUN npm ci

# 소스 코드 복사
COPY . .

# 포트 노출
EXPOSE 5173

# 개발 서버 실행
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
