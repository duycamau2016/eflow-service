# ─── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml trước để cache dependencies (tránh re-download khi chỉ thay đổi src)
COPY pom.xml .
RUN mvn dependency:resolve dependency:resolve-plugins -B --no-transfer-progress || true

# Copy source và build
COPY src ./src
RUN mvn package -DskipTests -B --no-transfer-progress

# ─── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy JAR từ stage build
COPY --from=build /app/target/eflow-service-1.0.0.jar app.jar

# Khai báo port (Railway đọc EXPOSE để biết port mặc định)
EXPOSE 8080

# Kích hoạt production profile
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
