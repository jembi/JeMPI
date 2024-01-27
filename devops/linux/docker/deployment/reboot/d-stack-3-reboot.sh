#!/bin/bash

set -e
set -u

echo
echo "Down stack"
source ../scripts/d-stack-rm.sh

echo
echo Up app containers
source ../scripts/d-stack-wait-removed.sh
sleep 2
source ../scripts/d-stack-deploy-0.sh
sleep 2
source ../scripts/d-stack-up-hub-containers.sh
sleep 2
source ../scripts/d-stack-up-keycloak-containers.sh
sleep 2
source ../scripts/d-stack-up-app-containers.sh
