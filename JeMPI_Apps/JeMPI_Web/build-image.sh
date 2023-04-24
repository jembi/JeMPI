#!/bin/bash
TAG_NAME=${1:-latest}
docker build -t jembi/jempi-web:$TAG_NAME .
