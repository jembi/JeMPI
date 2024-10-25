#!/usr/bin/bash

### export USE_LOCAL_REGISTRY=${USE_LOCAL_REGISTRY:-"true"}
### export PROJECT_PATH_APPS_ROOT=$(builtin cd ../../../../../; pwd)
### export PROJECT_PATH_UI=${PROJECT_PATH_APPS_ROOT}/JeMPI_Apps/JeMPI_UI
export PROJECT_DIR=$(builtin cd ../../; pwd)
### export PROJECT_DATA_DIR=${PROJECT_DIR}/docker_data/data
### export PROJECT_DATA_APPS_DIR=${PROJECT_DIR}/docker_data/data-apps
### export PROJECT_DATA_MONITOR_DIR=${PROJECT_DIR}/docker_data/data-monitor
### 
export NODE1=$(hostname)
#export NODE1_IP=$(ifconfig | grep -E "([0-9]{1,3}\.){3}[0-9]{1,3}" | grep -v 127.0.0.1 | head -1 | awk '{ print $2 }')
#export NODE1_IP=$(ip addr show eth0 | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
export NODE1_IP=$(hostname -i)
### 
### export POSTGRESQL_USERNAME="postgres"
### export POSTGRESQL_PASSWORD="postgres"
### export POSTGRESQL_DATABASE="postgres"
### export POSTGRESQL_USERS_DB="users_db"
### export POSTGRESQL_NOTIFICATIONS_DB="notifications_db"
### export POSTGRESQL_AUDIT_DB="audit_db"
### export POSTGRESQL_KC_TEST_DB="kc_test_db"
### 
### export KAFKA_SERVERS="kafka-01:9092"
### export DGRAPH_HOSTS="alpha-01"
### export DGRAPH_PORTS="9080"
### 
### # Ports
### export API_HTTP_PORT=50000
### export BACKUP_RESTORE_API_HTTP_PORT=50000
### export API_KC_HTTP_PORT=50000
### export ETL_HTTP_PORT=50000
### export CONTROLLER_HTTP_PORT=50000
### export LINKER_HTTP_PORT=50000
### 
export KC_REALM_NAME="jempi-dev"
### export KC_API_URL="http://keycloak-test-server:8080"
export KC_JEMPI_CLIENT_ID="jempi-oauth"
### export KC_JEMPI_CLIENT_SECRET="Nsuhj2lQiCgSE7eVPLBgnLEEeaijufeh"
### export KC_JEMPI_ROOT_URL="http://localhost:3000"
### export JEMPI_SESSION_SECRET="c05ll3lesrinf39t7mc5h6un6r0c69lgfno69dsak3vabeqamouq4328cuaekros401ajdpkh60rrt"
export JEMPI_FILE_IMPORT_MAX_SIZE_BYTE=10485760
### # Deployment related env vars
### export JEMPI_SESSION_SECURE="false"
### export JEMPI_SESSION_DOMAIN_NAME="localhost"
### 
# UI env vars
# NODE_ENV production || development
export NODE_ENV="production"
### export REACT_APP_JEMPI_BASE_API_HOST=http://${NODE1_IP}
### export REACT_APP_JEMPI_BASE_API_PORT=50000
export REACT_APP_MOCK_BACKEND="false"
export REACT_APP_ENABLE_SSO="false"
export KC_FRONTEND_URL=http://${NODE1_IP}:8080
### 
### # DON'T CHANGE
### export REGISTRY_NODE_IP=${NODE1}:5000/v2
### 
### if [ $USE_LOCAL_REGISTRY == 'true' ]; then
###     export IMAGE_REGISTRY="$REGISTRY_NODE_IP/"
### else
###     export IMAGE_REGISTRY=""
### fi

envsubst < config.template > conf.env
