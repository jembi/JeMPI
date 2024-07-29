#!/bin/bash

set -e
set -u

echo
echo "Down stack"
source ./helper/scripts/d-stack-08-rm.sh
source ./helper/scripts/d-stack-09-wait-removed.sh

echo
echo "Up app containers"
sleep 2
source ./helper/scripts/d-stack-02-deploy-0.sh
sleep 2
source ./helper/scripts/d-stack-03-up-hub-containers.sh

