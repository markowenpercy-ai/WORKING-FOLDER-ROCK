#!/bin/bash
set -e

cd "$(dirname "$0")"

# Load env vars (falls back to .env.example defaults)
set -a
[ -f .env ] && source .env
set +a

REGISTRY="${DOCKER_REGISTRY:-docker.io}"
USERNAME="${DOCKER_USERNAME}"
PASSWORD="${DOCKER_PASSWORD}"
IMAGE="${SERVER_IMAGE:-Xerovoxx98/OpenGalaxy-Server}"
VERSION="${SERVER_VERSION:-latest}"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)

echo "=== Logging into registry ${REGISTRY} ==="
echo "$PASSWORD" | docker login "$REGISTRY" -u "$USERNAME" --password-stdin

echo "=== Building Server Docker Image ==="
docker build -t opengalaxy-server:latest .

echo "=== Tagging Image ==="
FULL_IMAGE="${REGISTRY}/${IMAGE}"
docker tag opengalaxy-server:latest "${FULL_IMAGE}:${VERSION}"
docker tag opengalaxy-server:latest "${FULL_IMAGE}:${TIMESTAMP}"
docker tag opengalaxy-server:latest "${FULL_IMAGE}:latest"

echo "=== Pushing Image ==="
docker push "${FULL_IMAGE}:${VERSION}"
docker push "${FULL_IMAGE}:${TIMESTAMP}"
docker push "${FULL_IMAGE}:latest"

echo "=== Server build and push complete: ${FULL_IMAGE}:${VERSION} ==="