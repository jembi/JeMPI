#!/bin/bash
source ../conf.env
# Load Database Credentials from Environment Variables

DB_NAME="${POSTGRESQL_DATABASE}"
DB_USER="${POSTGRESQL_USERNAME}"
DB_PASSWORD="${POSTGRESQL_PASSWORD}"
DB_HOST="${POSTGRES_HOST:-localhost}"
BACKUP_PATH="${POSTGRES_BACKUP_DIRECTORY}/$(date +%Y%m%d_%H%M%S)"
OLD_LOGS_DIR="${BACKUP_PATH}/old_logs" # Directory to store old logs

databases=("$POSTGRESQL_DATABASE" "$POSTGRESQL_USERS_DB" "$POSTGRESQL_NOTIFICATIONS_DB" "$POSTGRESQL_AUDIT_DB" "$POSTGRESQL_KC_TEST_DB")

# Check and Create Backup Directory and Old Logs Directory
[ ! -d "$BACKUP_PATH" ] && mkdir -p "$BACKUP_PATH"
[ ! -d "$OLD_LOGS_DIR" ] && mkdir -p "$OLD_LOGS_DIR"

LOG_FILE="${BACKUP_PATH}/$(date +%Y%m%d_%H%M%S).log"

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
        backup_file="${BACKUP_PATH}/${db}--$(date +%Y%m%d_%H%M%S).sql"

        echo "$(date) - Starting backup for database: ${db}" >> "${LOG_FILE}"
        PGPASSWORD=$DB_PASSWORD pg_dump -h $DB_HOST -U $DB_USER -d $db -F c -f "${BACKUP_PATH}/${db}--$(date +%Y%m%d_%H%M%S).sql"
        echo "$(date) - Backup completed for database: ${db}" >> "${LOG_FILE}"
    done
    echo "Database Postgres Backup completed."
}

echo Function to Copy Backup to Remote Server
copy_to_remote() {
    if [ -n "${REMOTE_SERVER}" ] && [ -n "${REMOTE_PATH}" ]; then
        for db in "${databases[@]}"; do
            echo "$(date) - Starting remote transfer" >> "${LOG_FILE}"
            scp "${BACKUP_PATH}/${db}_$(date +%Y%m%d_%H%M%S).sql" ${REMOTE_SERVER}:${REMOTE_PATH}
            echo "$(date) - Remote transfer completed" >> "${LOG_FILE}"
        done
    else
        echo "$(date) - Remote server details not set. Skipping remote transfer." >> "${LOG_FILE}"
    fi
}

# # Main Execution
backup_database
copy_to_remote
