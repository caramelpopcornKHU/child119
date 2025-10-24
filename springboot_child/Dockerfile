# --- 1. 빌드 스테이지 (Build Stage) ---
# [수정됨] 더 표준적인 Eclipse Temurin(JDK 17) + Maven 3.9 이미지 사용
FROM maven:3.9-eclipse-temurin-17-slim AS builder

# 작업 디렉터리 설정
WORKDIR /app

# 먼저 pom.xml만 복사하여 의존성을 다운로드 (소스 코드가 바뀌지 않으면 이 레이어는 캐시됨)
COPY pom.xml .
RUN mvn dependency:go-offline

# 이제 나머지 소스 코드를 복사
COPY src ./src

# Maven으로 .jar 파일 빌드
# [참고] target/springboot_child-0.0.1-SNAPSHOT.jar 이름이 맞는지 확인하세요.
RUN mvn package


# --- 2. 실행 스테이지 (Runtime Stage) ---
# [수정됨] 더 표준적인 Eclipse Temurin(JRE 17) 경량 이미지 사용
FROM eclipse-temurin:17-jre-slim

# 작업 디렉터리 설정
WORKDIR /app

# 1번 빌드 스테이지('builder')에서 생성된 .jar 파일을 복사해옴
COPY --from=builder /app/target/springboot_child-0.0.1-SNAPSHOT.jar app.jar

# 8080 포트 노출 (Cloud Run이 $PORT 환경 변수를 주입하여 이 포트를 사용함)
EXPOSE 8080

# 최종 실행 명령어
ENTRYPOINT ["java", "-jar", "/app/app.jar"]