#!/bin/bash

set -m
set -m

docker exec $(docker ps -q -f name=keycloak-test-server) cat realm.json > export-jempi-dev-realm.json
