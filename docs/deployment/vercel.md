# Vercel 배포 가이드 (Frontend)

## 1. 프로젝트 연결
1. Vercel 대시보드에서 **New Project** → Git Provider 선택.
2. `frontend` 디렉터리를 프로젝트 루트로 지정(Framework Preset: Vite).
3. Build & Output 설정
   - `Build Command`: `npm run build`
   - `Output Directory`: `dist`
   - `Install Command`: `npm install` 또는 `pnpm install`

## 2. 환경 변수
필수 변수:

| Key | Value 예시 | 설명 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `https://<your-backend-domain>/api` | 백엔드 API 엔드포인트 |

> Vercel은 빌드 타임에 `import.meta.env`를 치환하므로, **Production** / **Preview** / **Development** 환경에 동일하게 설정하세요.

## 3. SPA 라우팅(404) 대응
Vue Router가 history 모드(`createWebHistory`)이므로, `/raid` 같은 경로로 직접 진입/새로고침하면 Vercel이 정적 파일을 찾지 못해 404가 날 수 있습니다.

- 해결: 모든 경로를 `index.html`로 rewrite
- 설정 파일: `frontend/vercel.json`
- Vercel Project 설정에서 Root Directory가 `frontend`인지 확인하세요.

## 4. 빌드/배포 파이프라인
1. PR 생성 시 Vercel Preview가 자동 빌드되어 API 베이스 URL이 동일하게 적용됩니다.
2. main/master 머지 시 Production URL로 승격됩니다.

## 5. 향후 플랫폼 변경 팁
- Frontend는 순수 정적 자산(`dist`)이므로 S3+CloudFront, Netlify, Cloudflare Pages 등으로 옮길 때도 동일한 `npm run build`와 `VITE_API_BASE_URL`만 필요합니다.
- `frontend/Dockerfile`은 자체 Nginx 배포가 필요한 경우에만 사용하고, Vercel에서는 무시됩니다.
