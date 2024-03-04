#!/bin/bash
source ../conf.env
#Backup Folder Name
while true; do
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

pushd ../deployment/reboot 
    source d-stack-3-reboot.sh
popd
echo "Please wait for sometime..."
sleep 60
# BACKUP_FOLDER_NAME="20240202_123952"
# Load Environment Variables for DGraph Alpha and Zero Nodes
DGRAPH_ALPHA_HOSTS="${DGRAPH_HOSTS:-localhost}"
DGRAPH_ALPHA_PORTS="${DGRAPH_PORTS:-8080}"
DGRAPH_ZERO_HOSTS="${DGRAPH_ZERO_HOSTS:-localhost}"
DGRAPH_ZERO_PORTS="${DGRAPH_ZERO_PORTS:-5080}"

# Load Environment Variables for Data Directories
DATA_DGRAPH_ZERO_01_DIR="${DATA_DGRAPH_ZERO_01_DIR}"
DATA_DGRAPH_ALPHA_01_DIR="${DATA_DGRAPH_ALPHA_01_DIR}"
DATA_DGRAPH_ALPHA_02_DIR="${DATA_DGRAPH_ALPHA_02_DIR:-}" # Optional
DATA_DGRAPH_ALPHA_03_DIR="${DATA_DGRAPH_ALPHA_03_DIR:-}" # Optional
BACKUP_PATH="${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"

REMOTE_SERVER="${DGRAPH_BACKUP_REMOTE_SERVER:-""}"
REMOTE_PATH="${DGRAPH_BACKUP_REMOTE_PATH:-""}"
# Function to Restore DGraph Directory
restore_dgraph_dir() {
    local dir=$1
    local backup_file=$2
    echo $dir
    if [ "$(ls -A $dir)" ]; then
        echo "Directory $dir is not empty. Clearing existing files."
        sudo rm -rf $dir/*
    fi

    echo “$(date) - Starting restore for DGraph directory at $dir from $backup_file”
    sudo tar -xzvf ${backup_file} -C $dir --strip-components=1
    echo “$(date) - Restore completed for DGraph directory at $dir”
}
# Restore Zero Nodes
for backup_file in ${BACKUP_PATH}/zero*.tar.gz; do
    echo $backup_file
    # Assuming the first directory is for zero nodes
    restore_dgraph_dir $DATA_DGRAPH_ZERO_01_DIR $backup_file
done
# Restore Alpha Nodes
for backup_file in ${BACKUP_PATH}/alpha*.tar.gz; do
    # Assuming the first directory is for alpha nodes
    restore_dgraph_dir $DATA_DGRAPH_ALPHA_01_DIR $backup_file
done

copy_from_remote() {
    if [ -n "${REMOTE_SERVER}" ] && [ -n "${REMOTE_PATH}" ]; then
        echo "$(date) - Starting remote copy"
        scp ${REMOTE_SERVER}:${REMOTE_PATH}/*_$(date +%Y%m%d).tar.gz "${BACKUP_PATH}/"
        echo "$(date) - Remote copy completed"
    else
        echo "$(date) - Remote server details not set. Skipping remote copy."
    fi
}
# Main Execution
copy_from_remote
echo "Rebooting JeMPI"
pushd ../deployment/reboot 
    echo $PWD
    source d-stack-3-reboot.sh
popd