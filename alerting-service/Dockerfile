FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ✅ Create non-root system user and group
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

COPY --from=build --chown=appuser:appgroup /app/target/*.jar app.jar

# ✅ Run as non-root user
USER appuser

EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
