# FreeDB (MariaDB) 사용 가이드

## 1. 데이터베이스 생성
1. https://freedb.tech 또는 사용 중인 FreeDB 제공업체에 로그인합니다.
2. 새 MariaDB/MySQL 데이터베이스를 생성하고 다음 정보를 확보합니다.
   - 호스트(예: `sql.freedb.tech`)
   - 포트(기본 3306)
   - DB 이름
   - 사용자/비밀번호

## 2. Spring Boot 연결
`SPRING_DATASOURCE_URL` 예시:

```
jdbc:mariadb://sql.freedb.tech:3306/<db_name>?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC
```

`.env.example`의 기본 값 대신 Railway 환경 변수 또는 로컬 `.env`에 직접 입력하세요.

## 3. 마이그레이션/초기 스키마
- `spring.jpa.hibernate.ddl-auto=update`가 기본으로 설정되어 있어 최초 실행 시 테이블이 생성됩니다.
- 운영 DB에서는 Schema Migration 도구(Flyway, Liquibase)로 전환할 것을 권장합니다.

## 4. 로컬 개발 시 주의
- FreeDB는 연결 제한이 있으므로 개발 단계에서는 로컬 MariaDB(Compose) 또는 Docker의 `database` 서비스를 사용하세요.
- 운영 DB 비밀번호를 저장소에 커밋하지 마세요. Railway / GitHub Actions Secret을 사용합니다.

## 5. 다른 DB로 이전할 때
- `SPRING_DATASOURCE_URL`과 드라이버만 교체하면 RDS, Planetscale, Supabase 등 다른 Managed DB로 손쉽게 이동할 수 있습니다.
