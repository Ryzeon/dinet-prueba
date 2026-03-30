FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

RUN ./mvnw dependency:go-offline -B 2>/dev/null || true

COPY src ./src

RUN ./mvnw clean package

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /app/target/dinet-prueba-*.jar app.jar

RUN useradd -m -u 1000 appuser || true

USER 1000

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

