# Stage 1: Build JAR using Maven with Eclipse Temurin 21 JDK
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build

WORKDIR /app

# Cache Maven dependencies
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Production JRE Runtime
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for container healthcheck probe
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Create secure non-root application user
RUN groupadd -r watchspaces && useradd -r -g watchspaces -d /app -s /sbin/nologin watchspaces

# Copy compiled jar from build stage
COPY --from=build /app/target/netflix-ai-watch-spaces-0.0.1-SNAPSHOT.jar app.jar
RUN chown -R watchspaces:watchspaces /app

USER watchspaces

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
