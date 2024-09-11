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

## Prerequisites
- PostgreSQL Version: Ensure that PostgreSQL 15.5.0 is installed. 
- The backup and restore operations are validated on this version.
Verify the installation by running psql --version

**Python Installation**
- Make sure Python and the python-dotenv package are installed to manage environment variables.
- Verify the installation by running python3 -m dotenv --version

**Using python-dotenv**
 Load these variables using python-dotenv:
- from dotenv import load_dotenv
- import os
- load_dotenv(/path/to/your/.env)
- print(os.getenv(environment variable name))
- python3 test_dotenv.py (This should load the environment variables from your .env.local file)

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

**Manual Backup Process**

- pg_dump -U <username> -d <database_name> > /path/to/backup_file.sql

**Verify if process was successful**
- echo $? (This variable holds the exit status of the last command executed. An exit status of 0 indicates that the last command (pg_dump) completed successfully.)
- ls -lh 

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