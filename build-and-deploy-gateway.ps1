Write-Host "Costruisco l'immagine Docker per dfp-piao-gateway..."
Push-Location dfp-piao-gateway
docker build -t piao-gateway:0.0.0 -f Dockerfile .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Build gateway fallita" }

docker tag piao-gateway:0.0.0 localhost:8443/dfp-docker-repo/dfp-piao-gateway:0.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-gateway:0.0.0

Write-Host "Carico immagine su minikube..."
minikube image load piao-gateway:0.0.0
minikube ssh "sudo docker tag piao-gateway:0.0.0 host.minikube.internal:8443/dfp-docker-repo/dfp-piao-gateway:0.0.0"
