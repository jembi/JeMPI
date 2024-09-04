---
description: Enable backup and restore for Postgres and Dgraph datastore's
---

# Backup and Restore

## Overview
This Functionality provides detailed instructions on how to perform backup and restore operations using the JeMPI_BackupRestoreAPI. 

This is a dedicated application for handling both backup and restore operations. The scripts included in this application cover:
- Backup Dgraph using API and dump JSON file.
- Backup Postgres using SQL-Dump
- Restore Dgraph using API using JSON file.
- Restore Postgres using SQL-Dump 

## Backup Operations
1. Dump sql files for Postgresql.
2. Get all GoldenIds
3. For each GoldenId: \
a. Retrieve the golden record \
b. Get the list of Golden Record Source Ids \
c. Get the list of interactions \
d. Write the data to file (JSON)

## Backup Dgraph and Postgres Process
- **Backup Directory Dgraph:** \
JeMPI/devops/linux/docker/docker_data/data/backups/dgraph
- **Backup Directory Postgres:** \
JeMPI/devops/linux/docker/docker_data/data/backups/postgres
- **Deployment File:** local-deployment.sh
```bash
cd JeMPI/devops/linux/docker/deployment
./local-deployment.sh
Select Option 5: Backup Postgres & Dgraph
```
- **Backup Script Path:** \
JeMPI/devops/linux/docker/backup_restore/dgraph-backup-api.sh
- **Backup Script Logic Dgraph:** \
JeMPI/devops/linux/docker/backup_restore/dgraph-backup-api.py
- **Backup Script Logic Postgres:** \
JeMPI/devops/linux/docker/backup_restore/postgres-backup.sh

The backup process creates a folder with a timestamp. Inside this folder, backups are created for each Dgraph and Postgres.

![Backup Postgres and Dgraph](.gitbook/assets/14)

## Restore Dgraph and Postgres Process

- **Backup Directory:** JeMPI/devops/linux/docker/docker_data/data/backups/dgraph
- **Deployment File:** local-deployment.sh

```bash
cd JeMPI/devops/linux/docker/deployment
./local-deployment.sh
Select Option 6: Restore Postgres & Dgraph
```

- It will prompt for confirmation (yes/no) and list the recent 5 backup folders.
- **Enter the backup folder name** - it will start the restoration from the selected backup file.
- **Backup Directory Dgraph:** JeMPI/devops/linux/docker/docker_data/data/backups/dgraph
- **Backup Directory Postgres:** JeMPI/devops/linux/docker/docker_data/data/backups/postgres
- **Manual Backup Run Script:** ./restore-dgraph-postgres.sh {{ Folder_Name }}

![Restore Postgres and Dgraph](.gitbook/assets/15)