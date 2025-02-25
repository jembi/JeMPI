#!/bin/bash
source ../conf.env
# Load Database Credentials from Environment Variables
if [ -z "$1" ]; then
    echo "Error: No backup folder name provided."
    echo "Usage: $0 <backup_folder_name>"
    exit 1
fi
if [ -z "$1" ]; then
    # Argument is empty, so set current datetime
    BACKUP_DATE_TIME=$(date +%Y%m%d_%H%M%S)
else
    # Argument is provided, use it as datetime
    BACKUP_DATE_TIME=$1
fi
DB_NAME="${POSTGRESQL_DATABASE}"
DB_USER="${POSTGRESQL_USERNAME}"
DB_PASSWORD="${POSTGRESQL_PASSWORD}"
DB_HOST="${POSTGRES_HOST:-localhost}"
BACKUP_PATH="${POSTGRES_BACKUP_DIRECTORY}/$BACKUP_DATE_TIME"
OLD_LOGS_DIR="${BACKUP_PATH}/old_logs" # Directory to store old logs

databases=("$POSTGRESQL_DATABASE" "$POSTGRESQL_USERS_DB" "$POSTGRESQL_NOTIFICATIONS_DB" "$POSTGRESQL_AUDIT_DB" "$POSTGRESQL_KC_TEST_DB", "$POSTGRESQL_CONFIGURATION_DB")

# Check and Create Backup Directory and Old Logs Directory
[ ! -d "$BACKUP_PATH" ] && mkdir -p "$BACKUP_PATH"
[ ! -d "$OLD_LOGS_DIR" ] && mkdir -p "$OLD_LOGS_DIR"

LOG_FILE="${BACKUP_PATH}/$BACKUP_DATE_TIME.log"

# Check for Remote Server Details
REMOTE_SERVER="${POSTGRES_BACKUP_REMOTE_SERVER}"
REMOTE_PATH="${POSTGRES_BACKUP_REMOTE_PATH}"
PGPORT="${PGPORT:-5432}"

# Function to Perform Backup
backup_database() {
    echo "Starting Postgres database Backup..."
    # Loop through each database and dump it
    for db in "${databases[@]}"; do
        echo "db.. $db "
        backup_file="${BACKUP_PATH}/${db}--$BACKUP_DATE_TIME.sql"

        echo "$(date) - Starting backup for database: ${db}" >> "${LOG_FILE}"
        PGPASSWORD=$DB_PASSWORD pg_dump -h $DB_HOST -U $DB_USER -d $db -F c -f "${BACKUP_PATH}/${db}--$BACKUP_DATE_TIME.sql"
        echo "$(date) - Backup completed for database: ${db}" >> "${LOG_FILE}"
    done
    echo "Database Postgres Backup completed."
}

echo Function to Copy Backup to Remote Server
copy_to_remote() {
    if [ -n "${REMOTE_SERVER}" ] && [ -n "${REMOTE_PATH}" ]; then
        for db in "${databases[@]}"; do
            echo "$(date) - Starting remote transfer" >> "${LOG_FILE}"
            scp "${BACKUP_PATH}/${db}_$BACKUP_DATE_TIME.sql" ${REMOTE_SERVER}:${REMOTE_PATH}
            echo "$(date) - Remote transfer completed" >> "${LOG_FILE}"
        done
    else
        echo "$(date) - Remote server details not set. Skipping remote transfer." >> "${LOG_FILE}"
    fi
}

# # Main Execution
backup_database
copy_to_remote
