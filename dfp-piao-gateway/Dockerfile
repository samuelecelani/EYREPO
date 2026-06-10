########################################
# STAGE 1 - BUILD
########################################
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# La pipeline CI usa dfp-piao-gateway come build context, quindi copiamo
# soltanto il modulo gateway (pom auto-consistente con parent spring-boot-starter-parent)
COPY . .

# Caching delle dipendenze
RUN mvn -B -C dependency:go-offline

# Build Spring Boot
RUN mvn -B clean package -DskipTests

########################################
# STAGE 2 - RUNTIME
########################################
FROM amazoncorretto:21

WORKDIR /app

# OpenShift UID random
RUN mkdir -p /app && chgrp -R 0 /app && chmod -R g=u /app

########################################
# Certificati opzionali
########################################
COPY ./certs/ /certs/

RUN sh -c '\
  if [ -d "/certs" ] && [ "$(ls -A /certs 2>/dev/null)" ]; then \
    echo "Installing custom certificates..."; \
    for cert in /certs/*.crt; do \
      [ -f "$cert" ] || continue; \
      alias=$(basename "$cert" .crt); \
      echo "Importing $cert as $alias"; \
      keytool -importcert \
        -trustcacerts \
        -noprompt \
        -alias "$alias" \
        -file "$cert" \
        -keystore "$JAVA_HOME/lib/security/cacerts" \
        -storepass changeit; \
    done; \
  else \
    echo "No custom certificates found, skipping."; \
  fi'

########################################
# Copia il fat JAR dallo stage build
########################################
COPY --from=build /build/target/*.jar /app/app.jar

########################################
# Runtime
########################################
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
