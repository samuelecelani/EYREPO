# Script: build-and-deploy.ps1
# Build delle immagini e push sul registry Nexus (dfp-docker-repo).

$ErrorActionPreference = 'Stop'

# 0) Build Entity (jar locale per BE/BFF)
.\build-entity.ps1

# 1) Login al registry Nexus
docker login localhost:8443

# 2) Build immagini in sequenza e push su Nexus
.\build-and-deploy-gateway.ps1
.\build-and-deploy-be.ps1
.\build-and-deploy-bff.ps1
.\build-and-deploy-fe.ps1
.\build-and-deploy-sync.ps1
