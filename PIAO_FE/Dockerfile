#############################
# 1) STAGE DI BUILD ANGULAR
#############################
FROM node:22-alpine AS build
ARG CONFIGURATION=production

WORKDIR /app

# Copia i file di configurazione
COPY package.json package-lock.json ./

# Installazione dipendenze
RUN npm install --production=false

# Copia del codice dell'app
COPY . .

# Compilazione Angular
RUN npm run build -- --configuration $CONFIGURATION

#############################
# 2) STAGE DI PRODUZIONE NGINX NON-ROOT
#############################
FROM nginx:1.25-alpine

# Creazione directory cache e pid scrivibili da utente non-root
RUN mkdir -p /var/cache/nginx/client_temp /tmp \
    && chown -R 1001:1001 /var/cache/nginx /usr/share/nginx/html /tmp

# Copia dei file buildati nella directory statica di nginx
COPY --from=build /app/dist/browser /usr/share/nginx/html

# Copia configurazione nginx ottimizzata
COPY nginx.conf /etc/nginx/nginx.conf

# Espone porta
EXPOSE 8080

# Imposta utente non-root per OpenShift/Minikube
USER 1001

# Entrypoint già presente nell'immagine nginx
