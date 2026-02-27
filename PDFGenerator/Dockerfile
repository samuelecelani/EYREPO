# Stage 1: Build dell'applicazione con Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia tutto il progetto (necessario per risolvere i moduli Maven)
COPY . .

# Build solo del modulo PDFGenerator (Maven gestirà automaticamente le dipendenze interne)
RUN mvn clean package -pl PDFGenerator -am -DskipTests -B

# Stage 2: Runtime con Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia il jar eseguibile prodotto nel build stage
COPY --from=build /app/PDFGenerator/target/*.jar app.jar

# Espone la porta 9084
EXPOSE 9084

# Comando di avvio dell'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
