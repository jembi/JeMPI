#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env

  echo "{
    \"insecure-registries\":[\"${PLACEMENT_REGISTRY}:5000\"],
    \"metrics-addr\":\"0.0.0.0:9323\",
    \"experimental\":true,
    \"mtu\":1492
  }" > daemon.json

  chmod u=rw,g=,o= daemon.json

  sudo mv daemon.json /etc/docker/.

  sudo systemctl restart docker
  echo "sleep for 15 secs"
  sleep 15

popd
