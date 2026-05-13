#!/bin/bash
set -e

cd "$(dirname "$0")"

VERSION=$(date +%Y%m%d-%H%M%S)

echo "=== Building Server Docker Image ==="
docker-compose build opengalaxy-server

echo "=== Tagging Image ==="
docker tag opengalaxy-server:latest opengalaxy-server:${VERSION}
docker tag opengalaxy-server:latest opengalaxy-server:latest

echo "=== Deploying Server ==="
docker-compose up -d --no-deps opengalaxy-server

echo "=== Server deployed as version ${VERSION} ==="