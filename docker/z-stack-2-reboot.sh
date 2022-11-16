#!/bin/bash

set -e
set -u

envsubst <conf/prometheus/prometheus.template >conf/prometheus/prometheus.yml

echo
echo Down stack
./helper/scripts/d-stack-08-rm.sh
./helper/scripts/d-stack-09-wait-removed.sh

echo
echo Up app containers

./helper/scripts/d-stack-01-create-dirs.sh
sleep 2

./helper/scripts/d-stack-02-deploy-0.sh
sleep 2

./helper/scripts/d-stack-03-up-hub-containers.sh
sleep 2

./helper/scripts/d-stack-04-up-app-containers.sh
