########################################
# STAGE 1 - BUILD
########################################
FROM maven:3.9-eclipse-temurin-21 AS build

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

# OpenShift UID random compatibility
RUN mkdir -p /app && chgrp -R 0 /app && chmod -R g=u /app

########################################
# Certificati opzionali
########################################
COPY certs/ /certs/

RUN sh -c '\
  if [ -d "/certs" ] && [ "$(ls -A /certs 2>/dev/null)" ]; then \
    echo "Installing custom certificates..."; \
    for cert in /certs/*.crt /certs/*.pem; do \
      [ -f "$cert" ] || continue; \
      base=$(basename "$cert" | sed "s/\.[^.]*$//"); \
      count=$(grep -c "BEGIN CERTIFICATE" "$cert" 2>/dev/null || echo 0); \
      if [ "$count" -gt 1 ]; then \
        echo "Bundle detected ($count certs) in $cert, splitting..."; \
        idx=0; \
        csplit -z -f /tmp/cert_ -b "%02d.crt" "$cert" "/BEGIN CERTIFICATE/" "{*}" 2>/dev/null; \
        for part in /tmp/cert_*.crt; do \
          [ -f "$part" ] || continue; \
          alias="${base}-${idx}"; \
          echo "Importing $part as $alias"; \
          keytool -importcert -trustcacerts -noprompt \
            -alias "$alias" -file "$part" \
            -keystore "$JAVA_HOME/lib/security/cacerts" \
            -storepass changeit 2>/dev/null || true; \
          idx=$((idx+1)); \
        done; \
        rm -f /tmp/cert_*.crt; \
      else \
        echo "Importing $cert as $base"; \
        keytool -importcert -trustcacerts -noprompt \
          -alias "$base" -file "$cert" \
          -keystore "$JAVA_HOME/lib/security/cacerts" \
          -storepass changeit; \
      fi; \
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
