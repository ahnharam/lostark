FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Gradle Wrapper 및 빌드 파일만 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성만 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src src

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["./gradlew", "bootRun", "--no-daemon"]
