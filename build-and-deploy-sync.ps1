Write-Host "Costruisco l'immagine Docker per sync-pp..."
Push-Location dfp-sync-pp
docker build -t sync-pp:0.0.0 -f Dockerfile .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Build sync-pp fallita" }

docker tag sync-pp:0.0.0 localhost:8443/dfp-docker-repo/dfp-sync-pp:0.0.0
docker push localhost:8443/dfp-docker-repo/dfp-sync-pp:0.0.0

Write-Host "Carico immagine su minikube..."
minikube image load sync-pp:0.0.0
minikube ssh "sudo docker tag sync-pp:0.0.0 host.minikube.internal:8443/dfp-docker-repo/dfp-sync-pp:0.0.0"
