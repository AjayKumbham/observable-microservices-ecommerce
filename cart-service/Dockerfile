# ── Build stage ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 1. Copy ALL pom.xml files first — changes rarely, so this layer is cached
COPY pom.xml .
COPY api-gateway/pom.xml          api-gateway/
COPY user-service/pom.xml         user-service/
COPY product-service/pom.xml      product-service/
COPY cart-service/pom.xml         cart-service/
COPY order-service/pom.xml        order-service/
COPY payment-service/pom.xml      payment-service/
COPY notification-service/pom.xml notification-service/

# 2. Resolve ALL dependencies offline and cache them in a BuildKit mount.
#    This layer is only invalidated when a pom.xml changes — NOT on src/ changes.
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn dependency:go-offline -pl cart-service -am -q

# 3. Copy only this service's source code
COPY cart-service/src cart-service/src

# 4. Build using the cached local Maven repo
RUN --mount=type=cache,target=/root/.m2,sharing=locked \
    mvn clean package -pl cart-service -am -DskipTests

# ── Run stage ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/cart-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
