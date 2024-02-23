#!/bin/bash

set -e
set -u

CONFIG=$1
API_FILENAME="${CONFIG%.*}"-api.json
COMMAND="run $CONFIG"
sbt "$COMMAND"
rm -f config-api.json
cp $API_FILENAME config-api.json
