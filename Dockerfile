# 1단계: 빌드 (JDK 21 + Gradle)
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle* ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew clean bootJar --no-daemon -x test

# 2단계: 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV TZ=Asia/Seoul

RUN useradd -r -u 1001 appuser
COPY --from=builder /workspace/build/libs/*.jar app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
