#!/bin/bash
source ../conf.env
#Backup Folder Name

    BACKUP_DATE_TIME=$1
    SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
    cd ${SCRIPT_DIR}/..
    JEMPI_DOCKER_HOME=$PWD
    # JEMPI_HOME = $1
    down_dir="$JEMPI_DOCKER_HOME/deployment/down"
    reboot_dir="$JEMPI_DOCKER_HOME/deployment/reboot"
    backup_restore_dir="$JEMPI_DOCKER_HOME/backup_restore"

    python_cmd=$(which python3 || which python)
    echo $python_cmd
    Function to stop services
    stop_services() {
        pushd "$down_dir"
            echo "Stopping API service"
            source d-stack-stop-services.sh
        popd
    }
     # Function to start backup restore API service
    start_backup_restore_service() {
        pushd "$reboot_dir"
            echo "Starting Backup Restore API service"
            source d-stack-start-backup-restore-api-services.sh
        popd
    }

     start_services() {
        pushd "$reboot_dir"
            echo "Starting API service"
            source d-stack-start-services.sh
        popd
    }

    # Function to stop backup restore API service
    stop_backup_restore_service() {
        pushd "$down_dir"
            echo "Stopping Backup Restore API service"
            source d-stack-stop-backup-restore-api-services.sh
        popd
    }

while true; do
        echo "Backup API"
    # Ask the user to enter a folder name
    echo "Backup folder Path:- ${DGRAPH_BACKUP_DIRECTORY}"
    pushd ${DGRAPH_BACKUP_DIRECTORY}
        echo
        echo "Recent 5 Backups list"
        ls -lt --time=creation --sort=time | grep '^d' | tail -n 5
        echo
    popd
    read -p "Please enter your Dgraph Backup Folder Name: " BACKUP_FOLDER_NAME

    # Check if the folder exists
    if [ -d "${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME" ]; then
        echo "Folder '$BACKUP_FOLDER_NAME' exists!"
        break  # Exit the loop if the folder exists
    else
        echo "Folder '$BACKUP_FOLDER_NAME' does not exist, at ${DGRAPH_BACKUP_DIRECTORY}. "
        echo  "Please try again"
    fi
done

BACKUP_DIR="${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"
backup_data() {
        pushd "$backup_restore_dir"
            local dir=$1
            echo "$backup_restore_dir"
            echo "$dir"
            sleep 20
            echo "Started Restore through API"
            $python_cmd dgraph-restore-api.py $dir
            sleep 10
            # sudo bash dgraph-backup.sh
            # sudo bash postgres-backup.sh
        popd
    }

stop_services
start_backup_restore_service


for backup_file in ${BACKUP_DIR}/dgraph_backup*.json; do
    # Assuming the first directory is for alpha nodes
    echo "innnn"
    backup_data $backup_file
done

start_services
stop_backup_restore_service


echo $BACKUP_DIR
