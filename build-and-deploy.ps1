# Script: build_and_deploy.ps1

# Costruisco le immagini Docker in modo sequenziale

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-gateway..."
docker build -t dfp-gateway:1.0.0 "./dfp-gateway"

#docker builder prune -a -f

Write-Host "Costruisco l'immagine Docker per dfp-piao-fe..."
docker build -t dfp-performance-fe:1.0.0 "./dfp-piao-fe"

# Avvia docker-compose solo dopo il completamento delle build
Write-Host "Avvio docker-compose..."
docker-compose up -d

Write-Host "Deploy completato."
Read-Host "Clicca INVIO per continuare"

Write-Host "Pushing immagine su nexus..."

# 1) login (se l’utente è locale di Nexus: admin/****** oppure utenti applicativi)
docker login localhost:8081/dfp-docker-repo/

# 2) tag dell’immagine puntando al repository "docker-hosted"
docker tag dfp-gateway:1.0.0 localhost:8081/dfp-docker-repo/dfp-gateway:1.0.0

# 3) push
docker push localhost:8081/dfp-docker-repo/dfp-gateway:1.0.0


Write-Host "Pushed"