#!/bin/bash

set -e
set -u

echo
echo "Down stack"
source ./helper/scripts/d-stack-rm.sh
source ./helper/scripts/d-stack-wait-removed.sh

echo
echo "Up app containers"
# source ./helper/scripts/d-stack-create-dirs.sh
sleep 2
source ./helper/scripts/d-stack-deploy-0.sh
sleep 2
source ./helper/scripts/d-stack-up-hub-containers.sh
sleep 2
source ./helper/scripts/d-stack-up-app-containers.sh

