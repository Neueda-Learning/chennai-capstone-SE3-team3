FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy dependency descriptors first so source changes do not invalidate dependency resolution.
COPY backend/pom.xml backend/pom.xml
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -B -DskipTests dependency:go-offline

COPY backend/src backend/src
RUN --mount=type=cache,target=/root/.m2 mvn -f backend/pom.xml -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -S app && adduser -S -G app app

WORKDIR /app
COPY --from=build /workspace/backend/target/backend-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
