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

BACKUP_DIR="${POSTGRES_BACKUP_DIRECTORY}/$BACKUP_FOLDER_NAME"

echo $BACKUP_DIR
