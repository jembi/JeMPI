#!/bin/bash

set -e
set -u
pushd .
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
    # Function to stop services
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

    # Function to backup data
    backup_data() {
        pushd "$backup_restore_dir"
            sleep 20
            echo "Started Backup through API"
            $python_cmd dgraph-backup.py $BACKUP_DATE_TIME
            sleep 10
            # sudo bash dgraph-backup.sh
            # sudo bash postgres-backup.sh
        popd
    }

    # Function to start services
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

    stop_services
    start_backup_restore_service
    backup_data
    start_services
    stop_backup_restore_service
popd