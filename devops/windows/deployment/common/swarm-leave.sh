#!/bin/bash
# Set JEMPI_HOME environment variable
export JEMPI_HOME=$(pwd)
echo "Setting JEMPI_HOME to: $JEMPI_HOME"​
cd $JEMPI_HOME/devops/windows/base-docker-wsl
echo "Leaving Swarm on node1"
source $JEMPI_HOME/devops/windows/base-docker-wsl/b-swarm-3-leave.sh