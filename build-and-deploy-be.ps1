Write-Host "Costruisco l'immagine Docker per dfp-piao-be..."
Push-Location dfp-piao-be
docker build -t piao-be:0.0.0 -f Dockerfile .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Build be fallita" }

docker tag piao-be:0.0.0 localhost:8443/dfp-docker-repo/dfp-piao-be:0.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-be:0.0.0

Write-Host "Carico immagine su minikube..."
minikube image load piao-be:0.0.0
minikube ssh "sudo docker tag piao-be:0.0.0 host.minikube.internal:8443/dfp-docker-repo/dfp-piao-be:0.0.0"
