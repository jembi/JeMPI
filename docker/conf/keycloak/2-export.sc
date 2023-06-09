#!/bin/bash

set -m
set -m

docker exec $(docker ps -q -f name=keycloak-test-server) kc.sh export --file realm.json --realm jempi-dev
docker exec $(docker ps -q -f name=keycloak-test-server) ls -la
