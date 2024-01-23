#!/bin/bash

cd ../../..
# Set environment variables
JEMPI_HOME=$(pwd)
JEMPI_IMAGE_BUILD_VERSION="0.3.1-test"
JAVA_VERSION="17.0.8.1_1"

# Function to build and push Docker image
build_and_push_docker_image() {
    local component="$1"
    local dockerfile_dir="$2"
    local image_name="jembi/jempi-$component:$JEMPI_IMAGE_BUILD_VERSION"

    echo "Building Docker image for $component"
    docker build --build-arg JAVA_VERSION=$JAVA_VERSION -t $image_name $dockerfile_dir

    echo "Pushing Docker image for $component"
    docker push $image_name
}

# Set JEMPI_HOME
echo "Setting JEMPI_HOME to: $JEMPI_HOME"

# Running JeMPI configuration
echo "Running JeMPI configuration"
cd "$JEMPI_HOME/JeMPI_Apps/JeMPI_Configuration/"
source create.sh "$JEMPI_HOME/JeMPI_Apps/JeMPI_Configuration/ethiopia/config-ethiopia-3.json"

# Build and push Docker images for each component
build_and_push_docker_image "async-receiver" "$JEMPI_HOME/JeMPI_Apps/JeMPI_AsyncReceiver/docker"
build_and_push_docker_image "controller" "$JEMPI_HOME/JeMPI_Apps/JeMPI_Controller/docker"
build_and_push_docker_image "linker" "$JEMPI_HOME/JeMPI_Apps/JeMPI_Linker/docker"
build_and_push_docker_image "api" "$JEMPI_HOME/JeMPI_Apps/JeMPI_API/docker"
build_and_push_docker_image "etl" "$JEMPI_HOME/JeMPI_Apps/JeMPI_ETL/docker"
build_and_push_docker_image "ui" "$JEMPI_HOME/JeMPI_Apps/JeMPI_UI/"
