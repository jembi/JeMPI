#!/bin/bash

# Load Backup Directory from Environment Variable
BACKUP_PATH="${POSTGRES_BACKUP_DIRECTORY}"
OLD_LOGS_DIR="${BACKUP_PATH}/old_logs"

# Create Logrotate Configuration File
cat << EOF > /etc/logrotate.d/postgres-backup
$BACKUP_PATH/backup_*.log {
    rotate 30
    daily
    missingok
    notifempty
    compress
    delaycompress
    create 640 root adm
    dateext
    dateformat -%Y%m%d
    olddir $OLD_LOGS_DIR
}
EOF

echo "Logrotate configuration for PostgreSQL backup updated."
