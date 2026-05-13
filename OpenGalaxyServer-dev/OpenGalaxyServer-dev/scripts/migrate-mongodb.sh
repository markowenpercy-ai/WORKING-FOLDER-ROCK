#!/bin/bash
set -e

BACKUP_DIR="./mongo_dump_$(date +%Y%m%d_%H%M%S)"
REMOTE_HOST="10.0.10.158"
REMOTE_USER="root"

echo "=== MongoDB Migration: Local -> Remote ==="
echo "Local MongoDB must remain RUNNING during this process"
echo ""

echo "=== Step 1: Creating backup directory ==="
mkdir -p "$BACKUP_DIR"

echo "=== Step 2: Dumping local MongoDB (runs live, no downtime) ==="
docker exec opengalaxy-mongodb mongodump \
  --uri="mongodb://opengalaxy:opengalaxy_dev_pass@localhost:27017/supergo2?authSource=admin" \
  --out="/tmp/dump" \
  --quiet

echo "=== Step 3: Copying dump from container to host ==="
docker cp opengalaxy-mongodb:/tmp/dump "$BACKUP_DIR/dump"

echo "=== Step 4: Cleaning up container temp files ==="
docker exec opengalaxy-mongodb rm -rf /tmp/dump

echo ""
echo "=== Dump complete ==="
echo "Backup location: $BACKUP_DIR/dump"
echo ""
echo "To restore to remote $REMOTE_HOST:"
echo "  scp -r $BACKUP_DIR/dump $REMOTE_USER@$REMOTE_HOST:/tmp/"
echo "  Then on remote run:"
echo "    mongorestore --uri='mongodb://opengalaxy:opengalaxy_dev_pass@localhost:27017/supergo2?authSource=admin' --drop /tmp/dump/dump"
echo ""
echo "Or if mongorestore is available locally via Docker:"
echo "  docker run --rm -v $BACKUP_DIR/dump:/dump mongo:6.0 mongorestore --uri='mongodb://opengalaxy:opengalaxy_dev_pass@$REMOTE_HOST:27017/supergo2?authSource=admin' --drop /dump/dump"