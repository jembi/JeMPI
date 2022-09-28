#!/bin/bash

export PROJECT_DIR=$(builtin cd ../../; pwd)
export PROJECT_DATA_DIR=${PROJECT_DIR}/docker_data/data
export PROJECT_DATA_APPS_DIR=${PROJECT_DIR}/docker_data/data-apps
export PROJECT_DATA_MONITOR_DIR=${PROJECT_DIR}/docker_data/data-monitor

export NODE1=DELL-BRUCED
export NODE1_IP=192.168.0.108


# DON'T CHANGE
export REGISTRY_NODE_IP=${NODE1}:5000/v2

envsubst < conf-env-1-pc.template > conf.env
#envsubst < pom-properties.template > ../../../pom.properties
