# Stage 1: Build dell'applicazione con Maven e Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia il file l e scarica le dipendenze in modo da sfruttare la cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia il codice sorgente
COPY src ./src

# Compila e package dell'applicazione saltando i test per velocizzare la build
RUN mvn package -DskipTests -B

# Stage 2: Runtime con Java 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia il jar eseguibile prodotto nel build stage
COPY --from=build /app/target/*.jar app.jar

# Espone la porta 8080
EXPOSE 8080

# Comando di avvio dell'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
