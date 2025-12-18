# 배포 개요

현재 기준 배포 구조는 다음과 같습니다.

- **Frontend**: Vercel (정적 Vite 빌드 업로드)
- **Backend**: Oracle VM (Docker Compose 기반 Spring Boot 컨테이너)
- **Database**: Oracle VM (Docker Compose의 MariaDB 컨테이너 + 볼륨)

## 환경 변수 매트릭스

| 변수 | Vercel Front | Oracle VM (.env) |
| --- | --- | --- |
| `VITE_API_BASE_URL` | ✅ (백엔드 URL) | 사용 안 함 |
| `LOSTARK_API_KEY` | 필요 없음 | ✅ |
| `DISCORD_BOT_TOKEN` | 필요 없음 | ✅ |
| `DISCORD_CLIENT_ID` | 필요 없음 | ✅ (OAuth 사용 시) |
| `DISCORD_CLIENT_SECRET` | 필요 없음 | ✅ (OAuth 사용 시) |
| `FRONTEND_BASE_URL` | 필요 없음 | ✅ (OAuth 리다이렉트 기준) |
| `CORS_ALLOWED_ORIGINS` | 필요 없음 | ✅ (콤마 구분, Origin과 정확히 일치) |
| `SESSION_COOKIE_SAME_SITE` | 필요 없음 | ✅ |
| `SESSION_COOKIE_SECURE` | 필요 없음 | ✅ |
| `CSRF_COOKIE_SAME_SITE` | 필요 없음 | (선택) |
| `CSRF_COOKIE_SECURE` | 필요 없음 | (선택) |
| `DB_ROOT_PASSWORD` | 필요 없음 | ✅ |
| `DB_USER` | 필요 없음 | ✅ |
| `DB_PASSWORD` | 필요 없음 | ✅ |

## Vercel 프론트 + 별도 백엔드(서로 다른 도메인)

프론트/백엔드 도메인이 다르면 브라우저가 쿠키를 3rd-party로 취급하므로, **POST/PUT/PATCH/DELETE 요청이 403(CSRF)** 로 막힐 수 있습니다.

백엔드 환경 변수에 아래 값을 설정하세요.

- `CORS_ALLOWED_ORIGINS=https://<your-vercel-domain>`
- `FRONTEND_BASE_URL=https://<your-vercel-domain>`
- `SESSION_COOKIE_SAME_SITE=None`
- `SESSION_COOKIE_SECURE=true`
- `CSRF_COOKIE_*`는 미설정 시 `SESSION_COOKIE_*` 값을 따라갑니다. (필요하면 명시적으로 덮어쓰기)
  - `CORS_ALLOWED_ORIGINS`는 Origin과 완전 일치해야 하므로 보통 끝 `/` 없이 설정합니다.

## 플랫폼별 가이드

- [Vercel (Frontend)](./vercel.md)
- [Oracle VM (Docker Compose + GitHub Actions)](./oracle-vm.md)
- (대안/참고) [Railway (Backend)](./railway.md)
- (대안/참고) [FreeDB (Database)](./freedb.md)

## 향후 플랫폼 변경을 대비하려면?

1. **환경 변수만으로 구성**: 애플리케이션은 인프라 이름을 알 필요가 없도록 모든 비밀/엔드포인트는 환경 변수로 전달합니다.
2. **Docs/Deployment 규약 유지**: 새 플랫폼을 추가할 때 동일한 형식의 문서를 추가하면 팀원이 참고하기 쉽습니다.
3. **Infra-agnostic Build**: Vercel/Vite, Railway/Docker에 묶이지 않도록 `npm run build`, `./gradlew bootJar` 등에 의존합니다.
4. **헬스체크/도메인 추상화**: 역프록시 주소나 API 엔드포인트는 `.env`의 `VITE_API_BASE_URL`, `SPRING_DATASOURCE_URL` 처럼 별도 값으로 관리해 향후 Cloud Run, Render 등으로 바뀌어도 `.env`만 교체하면 됩니다.
