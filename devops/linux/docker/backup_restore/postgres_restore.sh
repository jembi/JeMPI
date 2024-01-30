#!/bin/bash
source ../conf.env

# PostgreSQL settings from environment variables
DB_NAME="${POSTGRESQL_DATABASE}"
PGHOST="${POSTGRES_HOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGUSER="${POSTGRESQL_USERNAME}"
PGPASSWORD="${POSTGRESQL_PASSWORD}"
PGDATABASE="${POSTGRESQL_DATABASE}"

BACKUP_FOLDER_NAME="20240130_100908"
BACKUP_DIR="${POSTGRES_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"

databases=("$POSTGRESQL_DATABASE" "$POSTGRESQL_USERS_DB" "$POSTGRESQL_NOTIFICATIONS_DB" "$POSTGRESQL_AUDIT_DB" "$POSTGRESQL_KC_TEST_DB")


for db in "${databases[@]}"; do
    # Check if the database exists
    PGPASSWORD="$PGPASSWORD" psql -U "$PGUSER" -h "$PGHOST" -p $PGPORT -lqt | cut -d \| -f 1 | grep -qw "$db"

    if [ $? -ne 0 ]; then
        # Create the database if it doesn't exist
        PGPASSWORD="$PGPASSWORD" psql -U "$PGUSER" -h "$PGHOST" -p $PGPORT -c "CREATE DATABASE $db;" postgres
        echo "Database $db created."
    else
        echo "Database $db already exists."
    fi
    echo
done

echo "Restoring databases for "${databases[@]}""
echo
for backup_file in "$BACKUP_DIR"/*.sql; do
    
    if [ -f "$backup_file" ]; then
        DB_NAME=$(basename "$backup_file" | cut -d'-' -f1)
        echo "Restoring $DB_NAME with sql :- $backup_file"

        PGPASSWORD="$PGPASSWORD" pg_restore -U "$PGUSER" -h "$PGHOST" -d "$DB_NAME" -F c --clean --if-exists "$backup_file"

        echo "Restore completed for $DB_NAME"
    else
        echo "Backup file $backup_file not found."
    fi
    echo
done

echo "All backups restored. postgres"
