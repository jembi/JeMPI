#!/bin/bash
# Set JEMPI_HOME environment variable
export JEMPI_HOME=$(pwd)
echo "Setting JEMPI_HOME to: $JEMPI_HOME"​
# Check if Docker is already installed
if command -v docker &> /dev/null; then
    echo "Docker is already installed."
else
    # Install Docker
    sudo apt-get update
    sudo apt-get install -y docker.io

    # Add your user to the docker group to run Docker without sudo
    sudo usermod -aG docker $USER

    echo "Docker has been installed."
fi



# Navigate to environment configuration directory
echo "Navigate to environment configuration directory"
cd $JEMPI_HOME/devops/windows/base-docker-wsl/conf/env
dos2unix *
source $JEMPI_HOME/devops/windows/base-docker-wsl/conf/env/create-env-linux-low-1.sh

# Running Docker helper scripts 
echo "Running Docker helper scripts "
cd $JEMPI_HOME/devops/windows/base-docker-wsl/helper/scripts/
dos2unix *
source $JEMPI_HOME/devops/windows/base-docker-wsl/helper/scripts/x-swarm-a-set-insecure-registries.sh

cd $JEMPI_HOME

# Navigate to Docker directory

# Pull Docker images from hub
echo "Pull Docker images from hub"
cd $JEMPI_HOME/devops/windows/base-docker-wsl
source $JEMPI_HOME/devops/windows/base-docker-wsl/a-images-1-pull-from-hub.sh

if docker info | grep -q "Swarm: active"; then
    echo "Docker Swarm is running."
else
    echo "Docker Swarm is not running."
    echo "Initialize Swarm on node1"
    source $JEMPI_HOME/devops/windows/base-docker-wsl/b-swarm-1-init-node1.sh

fi

# Create Docker registry
echo "Create Docker registry"
source $JEMPI_HOME/devops/windows/base-docker-wsl/c-registry-1-create.sh

# Push Docker images to the registry
echo "Push Docker images to the registry"
source $JEMPI_HOME/devops/windows/base-docker-wsl/c-registry-2-push-hub-images.sh

# Build and reboot the entire stack
echo "Build and reboot the entire stack"
# yes | source $JEMPI_HOME/devops/windows/base-docker-wsl/z-stack-2-reboot-hub-images.sh
yes | source $JEMPI_HOME/devops/windows/base-docker-wsl/z-stack-2-reboot-hub-images-db-init.sh

