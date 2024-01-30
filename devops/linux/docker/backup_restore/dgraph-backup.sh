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
BACKUP_PATH="${DGRAPH_BACKUP_DIRECTORY}/$(date +%Y%m%d_%H%M%S)"
REMOTE_SERVER="${DGRAPH_BACKUP_REMOTE_SERVER}"
REMOTE_PATH="${DGRAPH_BACKUP_REMOTE_PATH}"

# Create Backup Directory if it doesn't exist
[ ! -d "$BACKUP_PATH" ] && mkdir -p "$BACKUP_PATH"

LOG_FILE="${BACKUP_PATH}/dgraph_backup_$(date +%Y%m%d_%H%M%S).log"

# Function to Backup DGraph Alpha and Zero Nodes
backup_dgraph_node() {
    local host=$1
    local port=$2

    echo "$(date) - Starting backup for DGraph node at ${host}:${port}" >> "${LOG_FILE}"
    # Replace with actual backup command for the node. Example:
    # curl "http://${host}:${port}/admin/backup"
    echo "$(date) - Backup completed for DGraph node at ${host}:${port}" >> "${LOG_FILE}"
}

# Function to Backup DGraph Directory
backup_dgraph_dir() {
    local dir=$1
    local dir_name=$(basename "$dir")

    echo "$(date) - Starting backup for DGraph directory at $dir" >> "${LOG_FILE}"
    tar -czvf "${BACKUP_PATH}/${dir_name}_$(date +%Y%m%d_%H%M%S).tar.gz" -C "$dir" .
    echo "$(date) - Backup completed for DGraph directory at $dir" >> "${LOG_FILE}"
}


# Backup DGraph Nodes (Alphas and Zeros)
IFS=',' read -r -a alpha_hosts <<< "$DGRAPH_ALPHA_HOSTS"
IFS=',' read -r -a alpha_ports <<< "$DGRAPH_ALPHA_PORTS"
IFS=',' read -r -a zero_hosts <<< "$DGRAPH_ZERO_HOSTS"
IFS=',' read -r -a zero_ports <<< "$DGRAPH_ZERO_PORTS"

for i in "${!alpha_hosts[@]}"; do
    backup_dgraph_node "${alpha_hosts[i]}" "${alpha_ports[i]}"
done

for i in "${!zero_hosts[@]}"; do
    backup_dgraph_node "${zero_hosts[i]}" "${zero_ports[i]}"
done

# Backup DGraph Directories
[ -d "$DATA_DGRAPH_ZERO_01_DIR" ] && backup_dgraph_dir "$DATA_DGRAPH_ZERO_01_DIR"
[ -d "$DATA_DGRAPH_ALPHA_01_DIR" ] && backup_dgraph_dir "$DATA_DGRAPH_ALPHA_01_DIR"
[ -d "$DATA_DGRAPH_ALPHA_02_DIR" ] && backup_dgraph_dir "$DATA_DGRAPH_ALPHA_02_DIR"
[ -d "$DATA_DGRAPH_ALPHA_03_DIR" ] && backup_dgraph_dir "$DATA_DGRAPH_ALPHA_03_DIR"

# Function to Copy Backup to Remote Server
copy_to_remote() {
    if [ -n "${REMOTE_SERVER}" ] && [ -n "${REMOTE_PATH}" ]; then
        echo "$(date) - Starting remote transfer" >> "${LOG_FILE}"
        scp "${BACKUP_PATH}/*_$(date +%Y%m%d).tar.gz" ${REMOTE_SERVER}:${REMOTE_PATH}
        echo "$(date) - Remote transfer completed" >> "${LOG_FILE}"
    else
        echo "$(date) - Remote server details not set. Skipping remote transfer." >> "${LOG_FILE}"
    fi
}

# Main Execution
copy_to_remote
chmod -R  777 "$BACKUP_PATH"
