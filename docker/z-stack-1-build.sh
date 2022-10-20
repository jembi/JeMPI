#!/bin/bash

set -e
set -u

echo
echo Build Apps
pwd
./helper/scripts/c-registry-3-build-push-app-images.sh
