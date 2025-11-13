# 배포 개요

현재 기준 배포 구조는 다음과 같습니다.

- **Frontend**: Vercel (정적 Vite 빌드 업로드)
- **Backend**: Railway (Dockerfile 기반 Spring Boot 컨테이너)
- **Database**: FreeDB (Hosted MariaDB)

## 환경 변수 매트릭스

| 변수 | Vercel Front | Railway Back | FreeDB |
| --- | --- | --- | --- |
| `VITE_API_BASE_URL` | ✅ (Railway 백엔드 URL) | 사용 안 함 | 사용 안 함 |
| `LOSTARK_API_KEY` | 필요 없음 | ✅ | 필요 없음 |
| `SPRING_DATASOURCE_URL` | 필요 없음 | ✅ (`jdbc:mariadb://<freedb-host>/<db>?useUnicode=true&characterEncoding=UTF-8`) | ✅ (관리 콘솔에서 제공) |
| `SPRING_DATASOURCE_USERNAME` | 필요 없음 | ✅ | ✅ |
| `SPRING_DATASOURCE_PASSWORD` | 필요 없음 | ✅ | ✅ |

## 플랫폼별 가이드

- [Vercel (Frontend)](./vercel.md)
- [Railway (Backend)](./railway.md)
- [FreeDB (Database)](./freedb.md)

## 향후 플랫폼 변경을 대비하려면?

1. **환경 변수만으로 구성**: 애플리케이션은 인프라 이름을 알 필요가 없도록 모든 비밀/엔드포인트는 환경 변수로 전달합니다.
2. **Docs/Deployment 규약 유지**: 새 플랫폼을 추가할 때 동일한 형식의 문서를 추가하면 팀원이 참고하기 쉽습니다.
3. **Infra-agnostic Build**: Vercel/Vite, Railway/Docker에 묶이지 않도록 `npm run build`, `./gradlew bootJar` 등에 의존합니다.
4. **헬스체크/도메인 추상화**: 역프록시 주소나 API 엔드포인트는 `.env`의 `VITE_API_BASE_URL`, `SPRING_DATASOURCE_URL` 처럼 별도 값으로 관리해 향후 Cloud Run, Render 등으로 바뀌어도 `.env`만 교체하면 됩니다.
