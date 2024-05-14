#!/bin/bash

set -e
set -u

CONFIG=$1
API_FILENAME="${CONFIG%.*}"-api.json
echo $API_FILENAME
rm -f config.json
ln -s $CONFIG config.json
cp $API_FILENAME config-api.json
cp -L -f $API_FILENAME ../../../../JeMPI_Apps/JeMPI_API/src/main/resources/config-api.json
cp -L -f $API_FILENAME ../../../../JeMPI_Apps/JeMPI_API_KC/src/main/resources/config-api.json
