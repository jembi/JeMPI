#!/bin/bash

set -e
set -u

if [ $# -eq 0 ]; then
    tag_to_use=""
else
    tag_to_use=$1
fi

tag_image() {
    if [ ! -z "$tag_to_use" ]; then
        IFS=':' read -a image_details <<< "$1"
        docker tag $1 ${image_details[0]}:$tag_to_use
    fi
}

pushd JeMPI_Configuration
  ./create.sh reference/config-reference.json 
popd

cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API/src/main/resources/config-api.json
cp -L -f ./JeMPI_Configuration/config-api.json ./JeMPI_API_KC/src/main/resources/config-api.json

mvn clean package
pushd JeMPI_EM_Scala
  sbt clean assembly
popd


pushd JeMPI_AsyncReceiver
  ./build.sh || exit 1
  tag_image $ASYNC_RECEIVER_HUB_IMAGE
popd
pushd JeMPI_ETL
  ./build.sh || exit 1
  tag_image $ETL_HUB_IMAGE
popd
pushd JeMPI_Controller
  ./build.sh || exit 1
  tag_image $CONTROLLER_HUB_IMAGE
popd
pushd JeMPI_EM_Scala
  ./build.sh || exit 1
  tag_image $EM_SCALA_HUB_IMAGE
popd
pushd JeMPI_Linker
  ./build.sh || exit 1
  tag_image $LINKER_HUB_IMAGE
popd
pushd JeMPI_API
  ./build.sh || exit 1
   tag_image $API_HUB_IMAGE
popd
pushd JeMPI_API_KC
  ./build.sh || exit 1
   tag_image $API_KC_HUB_IMAGE
popd
pushd JeMPI_Bootstrapper
  ./build.sh || exit 1
  tag_image $BOOTSTRAPPER_HUB_IMAGE
popd
pushd JeMPI_UI
  ./build-image.sh || exit 1
  tag_image $UI_HUB_IMAGE
popd