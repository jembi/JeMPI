#!/bin/bash

set -e
set -u

#echo
#echo Down stacks
#./helper/scripts/d-stack-08-rm.sh

echo
echo Build Apps
pwd
./helper/scripts/c-registry-3-build-push-app-images.sh

