# Railway 배포 가이드 (Backend)

## 1. 서비스 생성
1. Railway 대시보드 → **New Project** → **Deploy from GitHub**.
2. 저장소 연결 후 `backend` 폴더를 루트로 선택하거나 `railway.toml` 없이 **Monorepo** 모드에서 `backend` 경로를 지정합니다.
3. Build & Start Command
   - **Build**: `./gradlew bootJar --no-daemon`
   - **Start**: `java -jar build/libs/*.jar`
   - Railway가 Dockerfile 빌드를 지원하므로 `backend/Dockerfile`을 사용해도 됩니다. (Container → Docker 선택)

## 2. 환경 변수
Railway Environment 탭에 다음 값을 추가하세요. (FreeDB 정보는 [FreeDB 문서](./freedb.md) 참고)

| Key | Value 예시 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:mariadb://sql.freedb.tech:3306/<db>?useUnicode=true&characterEncoding=UTF-8` |
| `SPRING_DATASOURCE_USERNAME` | `<freedb-user>` |
| `SPRING_DATASOURCE_PASSWORD` | `<freedb-password>` |
| `LOSTARK_API_KEY` | `<onstove-api-key>` |
| `SPRING_PROFILES_ACTIVE` | `prod` (optional) |

> Railway는 Deploy 시점에 환경 변수를 자동 주입하므로 `.env`는 필요 없습니다.

## 3. 헬스 체크
- Railway Settings → Healthcheck Path에 `/actuator/health` (추후 Spring Actuator 추가 시) 또는 `/api/health`와 같은 단순 엔드포인트를 설정하면 자동 재시작이 가능합니다.

## 4. 향후 플랫폼 변경 팁
- Dockerfile 또는 JAR 실행 중 하나만 있으면 Render, Fly.io, AWS App Runner 등으로 쉽게 이전할 수 있습니다.
- 인프라가 바뀌어도 `SPRING_DATASOURCE_*`와 `LOSTARK_API_KEY`만 새 플랫폼의 Secret Manager에 등록하면 됩니다.
