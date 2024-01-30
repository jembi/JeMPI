#!/bin/bash
source ../conf.env
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
BACKUP_FOLDER_NAME="20240130_114844"
BACKUP_PATH="${DGRAPH_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"

REMOTE_SERVER="${DGRAPH_BACKUP_REMOTE_SERVER}"
REMOTE_PATH="${DGRAPH_BACKUP_REMOTE_PATH}"
# Function to Restore DGraph Directory

restore_dgraph_dir() {
    local dir=$1
    local backup_file=$2
    echo $backup_file
    echo $dir
    echo “$(date) - Starting restore for DGraph directory at $dir from $backup_file”
    tar -xzvf ${backup_file} -C $dir
    echo “$(date) - Restore completed for DGraph directory at $dir”
}
# Restore Zero Nodes
for backup_file in ${BACKUP_PATH}/zero*.tar.gz; do

    echo $backup_file
    echo "............"
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
whoami
copy_from_remote
# echo $PWD
# cd ../deployment/build_and_reboot
# echo $PWD
# yes | source d-stack-1-build-all-reboot.sh
