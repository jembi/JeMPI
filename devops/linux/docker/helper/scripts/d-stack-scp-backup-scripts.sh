#!/bin/bash

set -e
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf.env

  if [ "$SPEC_SETTINGS" == "cluster" ]; then
    pushd ../../backup_restore

      scp -r ./conf.env ${NODE2_USER}@${NODE2_IP}:${PROJECT_DIR}
      scp -r ./conf.env ${NODE2_USER}@${NODE2_IP}:${PROJECT_DIR}

      # NODE2
      scp -r ./dgraph-backup.sh ${NODE2_USER}@${NODE2_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./ddgraph-restore.sh ${NODE2_USER}@${NODE2_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./generate_logrotate_conf.sh ${NODE2_USER}@${NODE2_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./postgres-backup.sh ${NODE2_USER}@${NODE2_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./postgres-restore.sh ${NODE2_USER}@${NODE2_IP}:${BACKUP_RESTORE_DIR}

      #NODE3
      scp -r ./dgraph-backup.sh ${NODE3_USER}@${NODE3_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./ddgraph-restore.sh ${NODE3_USER}@${NODE3_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./generate_logrotate_conf.sh ${NODE3_USER}@${NODE3_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./postgres-backup.sh ${NODE3_USER}@${NODE3_IP}:${BACKUP_RESTORE_DIR}
      scp -r ./postgres-restore.sh ${NODE3_USER}@${NODE3_IP}:${BACKUP_RESTORE_DIR}
    popd
  fi
  echo
popd
