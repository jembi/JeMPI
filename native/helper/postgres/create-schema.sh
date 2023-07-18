#!/bin/bash

set -e 
set -u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./0-conf.env
  sudo -u postgres psql -tc "select 1 from pg_database where datname = 'notifications';" | grep -q 1 || sudo -u postgres psql -c "create database notifications;"
  sudo -u postgres psql -d "notifications" -a -f ./conf/postgres/config.sql
popd