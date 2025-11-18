# build-and-publish-to-dockerhub.ps1
param(
    [string]$Version = "2.2.5",
    [string]$AppName = "number-mnemocards-generator-bot"
)

$Tag = "${AppName}:$Version"
$DockerHubTag = "pechatny/${AppName}:$Version"
$DockerHubTagLatest = "pechatny/${AppName}:latest"
$OutputFile = "${AppName}-${Version}.tar"

Write-Host "Сборка Docker образа..." -ForegroundColor Green
docker build -t $Tag .

if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка сборки Docker образа!" -ForegroundColor Red
    exit 1
}

Write-Host "Присваиваю публичных тегов новой версии образа..." -ForegroundColor Green
docker tag $Tag $DockerHubTag
docker tag $Tag $DockerHubTagLatest

Write-Host "Отправка образа ${DockerHubTag} в dockerhub..." -ForegroundColor Green
docker push ${DockerHubTag}

Write-Host "Отправка образа ${DockerHubTagLatest} в dockerhub..." -ForegroundColor Green
docker push ${DockerHubTagLatest}

if ($LASTEXITCODE -ne 0) {
    Write-Host "Ошибка экспорта образа!" -ForegroundColor Red
    exit 1
}