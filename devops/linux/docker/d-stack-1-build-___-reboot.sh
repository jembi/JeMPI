#!/bin/bash

set -e
set -u

# Load env and docker image variables
source ./conf.env
source ./conf/images/conf-app-images.sh

# Define valid apps
valid_apps=("linker" "api" "api_kc" "asyncreceiver" "configuration" "controller" "etl" "libapi" "libmpi" "libshared")

# Initialize an array to hold invalid apps
invalid_apps=()

# Function to scale down, build, push, and scale up an app
process_app() {
  local app=$1
  local service="jempi_${app}"
  local name=$(docker ps -f name=$service --format "{{.Names}}")
  local scale=$(docker service inspect --format='{{.Spec.Mode.Replicated.Replicas}}' $service || echo 1)

  if [ -n "$name" ]; then
    echo "Scaling down $service"
    docker service scale $service=0
    docker wait $name
  fi

  # Find the directory name (case-insensitive)
  local app_dir=$(find ../../../JeMPI_Apps/ -maxdepth 1 -type d -iname "jempi_$app" | head -n 1)
  if [ -z "$app_dir" ]; then
    echo "Error: Directory for $app not found"
    exit 1
  fi

  # Delete JAR files in the target app docker directory
  find "$app_dir/docker" -maxdepth 1 -type f -name "*.jar" -delete

  pushd $app_dir
    echo "Path: $app_dir"
    echo "Building $app"
    ./build.sh || exit 1
  popd
  sleep 2

  echo "Pushing $app"
  local app_image_var="${app^^}_IMAGE"
  local app_image="${!app_image_var}"
  pushd $app_dir
    ./push.sh || exit 1
  popd
  sleep 2

  if [ -n "$name" ]; then
    echo "Updating $service to use the new image ($app_image)"
    docker service update --with-registry-auth --image $REGISTRY_NODE_IP/$app_image $service
    echo "Scaling up $service to $scale"
    docker service scale $service=$scale

    # Verify the image used by the service
    current_image=$(docker service inspect --format '{{.Spec.TaskTemplate.ContainerSpec.Image}}' $service)
    echo "Current image for $service: $current_image\n\n"
  fi
}

# Build the java stack once and deploy selected apps
pushd ../../../JeMPI_Apps
  mvn clean package
popd
sleep 1

for app in "$@"; do
  if [[ " ${valid_apps[@]} " =~ " ${app} " ]]; then
    process_app $app
  else
    invalid_apps+=($app)
  fi
done

docker stack services ${STACK_NAME}

# Notify user of invalid apps
if [ ${#invalid_apps[@]} -ne 0 ]; then
  echo "The following apps are not valid and were ignored: ${invalid_apps[@]}"
fi
