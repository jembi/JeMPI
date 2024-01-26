#!/bin/bash
source ../conf.env
# Load Database Credentials from Environment Variables
DB_NAME="${POSTGRESQL_DATABASE}"
DB_USER="${POSTGRES_USER}"
DB_PASSWORD="${POSTGRESQL_PASSWORD}"
DB_HOST="${POSTGRES_HOST:-localhost}"
BACKUP_PATH="${POSTGRES_BACKUP_DIRECTORY}"
OLD_LOGS_DIR="${BACKUP_PATH}/old_logs" # Directory to store old logs

# Check and Create Backup Directory and Old Logs Directory
[ ! -d "$BACKUP_PATH" ] && mkdir -p "$BACKUP_PATH"
[ ! -d "$OLD_LOGS_DIR" ] && mkdir -p "$OLD_LOGS_DIR"

LOG_FILE="${BACKUP_PATH}/backup_$(date +%Y%m%d).log"

# Check for Remote Server Details
REMOTE_SERVER="${POSTGRES_BACKUP_REMOTE_SERVER}"
REMOTE_PATH="${POSTGRES_BACKUP_REMOTE_PATH}"

# Function to Perform Backup
backup_database() {
    echo "$(date) - Starting backup for database: ${DB_NAME}" >> "${LOG_FILE}"
    PGPASSWORD=$DB_PASSWORD pg_dump -h $DB_HOST -U $DB_USER $DB_NAME > "${BACKUP_PATH}/backup_$(date +%Y%m%d).sql"
    echo "$(date) - Backup completed for database: ${DB_NAME}" >> "${LOG_FILE}"
}

# Function to Copy Backup to Remote Server
copy_to_remote() {
    if [ -n "${REMOTE_SERVER}" ] && [ -n "${REMOTE_PATH}" ]; then
        echo "$(date) - Starting remote transfer" >> "${LOG_FILE}"
        scp "${BACKUP_PATH}/backup_$(date +%Y%m%d).sql" ${REMOTE_SERVER}:${REMOTE_PATH}
        echo "$(date) - Remote transfer completed" >> "${LOG_FILE}"
    else
        echo "$(date) - Remote server details not set. Skipping remote transfer." >> "${LOG_FILE}"
    fi
}

# Main Execution
backup_database
copy_to_remote
