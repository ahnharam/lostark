# backend/Dockerfile

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# GitHub Actions에서 미리 빌드해 둔 JAR 복사
# 파일 이름 패턴에 맞게 수정 (예: lostark-backend-0.0.1-SNAPSHOT.jar)
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
