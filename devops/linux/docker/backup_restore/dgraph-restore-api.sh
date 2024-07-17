#!/bin/bash
source ../conf.env
#Backup Folder Name
    BACKUP_FOLDER_NAME=$1
    SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
    cd ${SCRIPT_DIR}/..
    JEMPI_DOCKER_HOME=$PWD
    # JEMPI_HOME = $1
    down_dir="$JEMPI_DOCKER_HOME/deployment/down"
    reboot_dir="$JEMPI_DOCKER_HOME/deployment/reboot"
    backup_restore_dir="$JEMPI_DOCKER_HOME/backup_restore"

    python_cmd=$(which python3 || which python)
    echo $python_cmd

     # Function to start backup restore API service
    start_backup_restore_service() {
        pushd "$reboot_dir"
            echo "Starting Backup Restore API service"
            source d-stack-start-backup-restore-api-services.sh
        popd
    }

    # Function to stop backup restore API service
    stop_backup_restore_service() {
        pushd "$down_dir"
            echo "Stopping Backup Restore API service"
            source d-stack-stop-backup-restore-api-services.sh
        popd
    }

BACKUP_DIR="${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"
restore_data() {
        pushd "$backup_restore_dir"
            local dir=$1
            echo "$backup_restore_dir"
            sleep 20
            echo "Started Restore through API"
            $python_cmd dgraph-restore-api.py $dir
            sleep 10
            # sudo bash dgraph-backup.sh
            # sudo bash postgres-backup.sh
        popd
    }

start_backup_restore_service

for backup_file in ${BACKUP_DIR}/dgraph_backup*.json; do
    # Assuming the first directory is for alpha nodes
    echo "Backup file found : $backup_file"
    restore_data $backup_file
done

stop_backup_restore_service
