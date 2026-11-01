#!/usr/bin/env bash
# build-and-publish-to-dockerhub.sh — сборка и публикация Docker-образа в Docker Hub (macOS).
#
# Использование:
#   ./build-and-publish-to-dockerhub.sh [версия] [имя_приложения]
#   PLATFORM=linux/arm64 ./build-and-publish-to-dockerhub.sh
#
# На Mac с Apple Silicon docker по умолчанию собирает arm64-образ, который не запустится
# на amd64-сервере. Поэтому платформа задаётся явно (по умолчанию linux/amd64).

set -euo pipefail

VERSION="${1:-2.4.0}"
APP_NAME="${2:-number-mnemocards-generator-bot}"
PLATFORM="${PLATFORM:-linux/amd64}"

TAG="${APP_NAME}:${VERSION}"
DOCKERHUB_TAG="pechatny/${APP_NAME}:${VERSION}"
DOCKERHUB_TAG_LATEST="pechatny/${APP_NAME}:latest"

GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

info() {
    printf "${GREEN}%s${NC}\n" "$1"
}

fail() {
    printf "${RED}%s${NC}\n" "$1" >&2
    exit 1
}

# Сборка из каталога скрипта, где лежит Dockerfile, откуда бы скрипт ни запускали
cd "$(dirname "$0")"

info "Сборка Docker образа ${TAG} для платформы ${PLATFORM}..."
docker build --platform "${PLATFORM}" -t "${TAG}" . || fail "Ошибка сборки Docker образа!"

info "Присваиваю публичные теги новой версии образа..."
docker tag "${TAG}" "${DOCKERHUB_TAG}"
docker tag "${TAG}" "${DOCKERHUB_TAG_LATEST}"

info "Отправка образа ${DOCKERHUB_TAG} в dockerhub..."
docker push "${DOCKERHUB_TAG}" || fail "Ошибка отправки образа ${DOCKERHUB_TAG}!"

info "Отправка образа ${DOCKERHUB_TAG_LATEST} в dockerhub..."
docker push "${DOCKERHUB_TAG_LATEST}" || fail "Ошибка отправки образа ${DOCKERHUB_TAG_LATEST}!"

info "Готово: ${DOCKERHUB_TAG} и ${DOCKERHUB_TAG_LATEST}"
