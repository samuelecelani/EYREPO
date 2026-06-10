Write-Host "Costruisco l'immagine Docker per dfp-piao-bff..."
Push-Location dfp-piao-bff
docker build -t piao-bff:0.0.0 -f Dockerfile .
Pop-Location
if ($LASTEXITCODE -ne 0) { throw "Build bff fallita" }

docker tag piao-bff:0.0.0 localhost:8443/dfp-docker-repo/dfp-piao-bff:0.0.0
docker push localhost:8443/dfp-docker-repo/dfp-piao-bff:0.0.0

Write-Host "Carico immagine su minikube..."
minikube image load piao-bff:0.0.0
minikube ssh "sudo docker tag piao-bff:0.0.0 host.minikube.internal:8443/dfp-docker-repo/dfp-piao-bff:0.0.0"

# Patch bff-cm: NOTIFICHE_SVC (richiesto dal BFF, non in services-cm)
$ns = "dfp-piao"
$bffPatch = '{"data":{"NOTIFICHE_SVC":"http://host.minikube.internal:9183"}}'
kubectl -n $ns patch cm bff-cm --type merge -p $bffPatch
