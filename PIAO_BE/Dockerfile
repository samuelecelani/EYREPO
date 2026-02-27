# Stage 1: Build dell'applicazione con Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia tutto il progetto (necessario per risolvere i moduli Maven)
COPY . .

# Build solo del modulo PIAO_BE (Maven gestirà automaticamente le dipendenze interne)
RUN mvn clean package -pl PIAO_BE -am -DskipTests -B

# Stage 2: Runtime con Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia il jar eseguibile prodotto nel build stage
COPY --from=build /app/PIAO_BE/target/*.jar app.jar

# Espone la porta 8080
EXPOSE 8080

# Comando di avvio dell'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
