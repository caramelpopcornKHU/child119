# --- 1. 빌드 스테이지 (Build Stage) ---
# 검증된 태그 (Maven 3.9.x + Java 17)
FROM maven:3-eclipse-temurin-17-slim AS builder

# 작업 디렉터리 설정
WORKDIR /app

# 먼저 pom.xml만 복사하여 의존성을 다운로드
COPY pom.xml .
RUN mvn dependency:go-offline

# 이제 나머지 소스 코드를 복사
COPY src ./src

# Maven으로 .jar 파일 빌드
RUN mvn package


# --- 2. 실행 스테이지 (Runtime Stage) ---
FROM eclipse-temurin:17-jre-slim

# 작업 디렉터리 설정
WORKDIR /app


COPY --from=builder /app/target/springboot_child-0.0.1-SNAPSHOT.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 최종 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]