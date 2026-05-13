#!/bin/bash
set -e

cd "$(dirname "$0")"

VERSION=$(date +%Y%m%d-%H%M%S)

echo "=== Building React Client ==="
cd client
npm run build
cd ..

echo "=== Building Client Docker Image ==="
docker-compose build opengalaxy-client

echo "=== Tagging Image ==="
docker tag opengalaxy-client:latest opengalaxy-client:${VERSION}
docker tag opengalaxy-client:latest opengalaxy-client:latest

echo "=== Deploying Client ==="
docker-compose up -d --no-deps opengalaxy-client

echo "=== Client deployed as version ${VERSION} ==="