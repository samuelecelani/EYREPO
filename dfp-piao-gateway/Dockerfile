########################################
# STAGE 1 - BUILD
########################################
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /build

# Copia solo pom.xml per caching delle dipendenze
COPY pom.xml .
RUN mvn -B -C dependency:go-offline

# Copia il codice sorgente
COPY src ./src

# Build Spring Boot
RUN mvn -B clean package -DskipTests

########################################
# Estraggo il fat JAR nello stage build
########################################
RUN cp target/*.jar app.jar
RUN jar -xf app.jar

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
COPY certs/ /certs/

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
# Copia classi e librerie dallo stage build
########################################
COPY --from=build /build/BOOT-INF ./BOOT-INF
COPY --from=build /build/META-INF ./META-INF

########################################
# Runtime
########################################
EXPOSE 8080

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:InitialRAMPercentage=50"

# Avvio della Start-Class con classpath manuale
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp 'BOOT-INF/classes:BOOT-INF/lib/*' it.sogei.dfp.gateway.GatewayApplication"]