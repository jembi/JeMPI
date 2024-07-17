#!/bin/bash
source ../conf.env
#Backup Folder Name
SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
cd ${SCRIPT_DIR}/.. || exit
JEMPI_DOCKER_HOME=$PWD
# JEMPI_HOME = $1
down_dir="$JEMPI_DOCKER_HOME/deployment/down"
reboot_dir="$JEMPI_DOCKER_HOME/deployment/reboot"
backup_restore_dir="$JEMPI_DOCKER_HOME/backup_restore"
RED='\033[0;31m'
NC='\033[0m'  # No Color


python_cmd=$(which python3 || which python)
echo $python_cmd
Function to stop services
stop_services() {
    pushd "$down_dir" || exit
        echo "Stopping API service"
        source d-stack-stop-services.sh
    popd || exit
}
start_services() {
    pushd "$reboot_dir" || exit
        echo "Starting API service"
        source d-stack-start-services.sh
    popd || exit
}

while true; do
        echo "Backup API"
    # Ask the user to enter a folder name
    echo "Backup folder Path:- ${DGRAPH_BACKUP_DIRECTORY}"
    pushd ${DGRAPH_BACKUP_DIRECTORY} || exit
        echo
        echo "Recent 5 Backups list"
        # ls -lt --time=creation --sort=time | grep '^d' | tail -n 5
        find . -mindepth 1 -type d -ctime -10 -exec sh -c 'find "$1" -mindepth 1 -print -quit | grep -q .' sh {} \; -exec stat --format '%Y %n' {} + | sort -nr | head -5 | awk '{print $2}'
        echo
    popd  || exit
    read -p "Please enter your Backup Folder Name: " BACKUP_FOLDER_NAME

    # Check if the folder exists
    if [ -d "${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME" ] && [ -d "${POSTGRES_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME" ]; then
        echo "Folder '$BACKUP_FOLDER_NAME' exists!"
        break  # Exit the loop if the folder exists
    else
        echo -e "${RED}Folder '$BACKUP_FOLDER_NAME' does not exist in either ${DGRAPH_BACKUP_DIRECTORY} or ${POSTGRES_BACKUP_DIRECTORY}.${NC}"
        echo -e "${RED}Please try again.${NC}"
    fi
done

BACKUP_DIR="$BACKUP_FOLDER_NAME"

if [ -n "$BACKUP_DIR" ]; then
    # Run the next command
    stop_services
    source helper/bootstrapper/bootstrapper-docker.sh data resetAll
    pushd "$JEMPI_DOCKER_HOME/backup_restore" || exit
        echo "Starting Dgraph database restore..."
        bash dgraph-restore-api.sh $BACKUP_DIR
        echo "Database Dgraph restore completed."
    popd || exit
    pushd "$JEMPI_DOCKER_HOME/backup_restore" || exit
        echo "Starting Postgres database restore..."
        sudo bash postgres-restore.sh $BACKUP_DIR
        echo "Database Postgres restore completed."
    popd || exit
    start_services

    # Replace this with the actual command you want to run
else
    echo -e "${RED}Enter valid Folder name.${NC}"
fi

