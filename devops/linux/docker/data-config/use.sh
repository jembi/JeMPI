#!/bin/bash

set -e
set -u

CONFIG=$1
API_FILENAME="${CONFIG%.*}"-api.json
echo $API_FILENAME
rm -f config.json
rm -f config-api.json
ln -s $CONFIG config.json
ln -s $API_FILENAME config-api.json
