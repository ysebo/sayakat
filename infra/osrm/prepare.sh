#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")"
mkdir -p data
cd data

PBF=kyrgyzstan-latest.osm.pbf
if [ ! -f "$PBF" ]; then
  curl -fL -o "$PBF" "https://download.geofabrik.de/asia/kyrgyzstan-latest.osm.pbf"
fi

IMAGE=osrm/osrm-backend:v5.27.1
docker run --rm -t -v "$PWD:/data" "$IMAGE" osrm-extract -p /opt/car.lua "/data/$PBF"
docker run --rm -t -v "$PWD:/data" "$IMAGE" osrm-partition /data/kyrgyzstan-latest.osrm
docker run --rm -t -v "$PWD:/data" "$IMAGE" osrm-customize /data/kyrgyzstan-latest.osrm

echo "OSRM data ready. Start it with: docker compose --profile osrm up -d osrm"
