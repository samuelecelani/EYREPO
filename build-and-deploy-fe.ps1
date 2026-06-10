Write-Host "Costruisco l'immagine Docker per dfp-piao-fe..."
Push-Location dfp-piao-fe
docker build -t piao-fe:0.0.0 -f Dockerfile .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Build fe fallita" }

docker tag piao-fe:0.0.0 localhost:8443/dfp-docker-repo/dfp-piao-fe:0.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-fe:0.0.0

Write-Host "Carico immagine su minikube..."
minikube image load piao-fe:0.0.0
minikube ssh "sudo docker tag piao-fe:0.0.0 host.minikube.internal:8443/dfp-docker-repo/dfp-piao-fe:0.0.0"
