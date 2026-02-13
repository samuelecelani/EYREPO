# Script: build_and_deploy.ps1

# Costruisco le immagini Docker in modo sequenziale

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-piao-gateway..."
docker build -t dfp-piao-gateway:1.0.0 "./dfp-piao-gateway"

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-piao-be..."
docker build -t dfp-piao-be:1.0.0 -f PIAO_BE/Dockerfile .

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-piao-bff..."
docker build -t dfp-piao-bff:1.0.0 -f PIAO_BFF/Dockerfile .

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-piao-fe..."
docker build -t dfp-piao-fe:1.0.0 -f PIAO_FE/Dockerfile .

# Avvia docker-compose solo dopo il completamento delle build
Write-Host "Avvio docker-compose..."
docker-compose up -d

Write-Host "Deploy completato."
Read-Host "Clicca INVIO per continuare"

Write-Host "Pushing immagine su nexus..."

# 1) login (se l’utente è locale di Nexus: admin/****** oppure utenti applicativi)
docker login localhost:8443/dfp-docker-repo/

# 2) tag dell’immagine puntando al repository "docker-hosted"
docker tag dfp-piao-gateway:1.0.0 localhost:8443/dfp-docker-repo/dfp-piao-gateway:1.0.0
docker tag dfp-piao-be:1.0.0 localhost:8443/dfp-docker-repo/dfp-piao-be:1.0.0
docker tag dfp-piao-bff:1.0.0 localhost:8443/dfp-docker-repo/dfp-piao-bff:1.0.0
docker tag dfp-piao-fe:1.0.0 localhost:8443/dfp-docker-repo/dfp-piao-fe:1.0.0

# 3) push
docker push localhost:8443/dfp-docker-repo/dfp-piao-gateway:1.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-be:1.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-bff:1.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-fe:1.0.0

Write-Host "Carico immagine su minikube..."

# 4) carica immagine su minikube
minikube image load dfp-piao-gateway:1.0.0
minikube image load dfp-piao-be:1.0.0
minikube image load dfp-piao-bff:1.0.0
minikube image load dfp-piao-fe:1.0.0

Read-Host "Pushed"


