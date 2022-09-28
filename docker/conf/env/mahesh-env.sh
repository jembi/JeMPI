#!/bin/bash

export PROJECT_DIR=$(builtin cd ../../; pwd)
export PROJECT_DATA_DIR=${PROJECT_DIR}/docker_data/data
export PROJECT_DATA_APPS_DIR=${PROJECT_DIR}/docker_data/data-apps
export PROJECT_DATA_MONITOR_DIR=${PROJECT_DIR}/docker_data/data-monitor

export NODE1=docker-desktop
export NODE1_IP=127.0.0.1

export ZOOKEEPER_LIMIT_MEMORY=256M    
export KAFKA_1_LIMIT_MEMORY=2G
export KAFKA_2_LIMIT_MEMORY=2G
export KAFKA_3_LIMIT_MEMORY=2G
export ZERO_LIMIT_MEMORY=1G
export ALPHA_1_LIMIT_MEMORY=5G
export ALPHA_2_LIMIT_MEMORY=5G
export ALPHA_3_LIMIT_MEMORY=5G
export RATEL_LIMIT_MEMORY=32M
export TEST_01_LIMIT_MEMORY=2G
export STAGING_01_LIMIT_MEMORY=2G
export CONTROLLER_LIMIT_MEMORY=2G
export EM_LIMIT_MEMORY=3G
export LINKER_LIMIT_MEMORY=3G
export API_LIMIT_MEMORY=1G

export ZOOKEEPER_RESERVATION_MEMORY=128M
export KAFKA_1_RESERVATION_MEMORY=512M
export KAFKA_2_RESERVATION_MEMORY=512M
export KAFKA_3_RESERVATION_MEMORY=512M
export ZERO_RESERVATION_MEMORY=512M
export ALPHA_1_RESERVATION_MEMORY=512M
export ALPHA_2_RESERVATION_MEMORY=512M
export ALPHA_3_RESERVATION_MEMORY=512M
export RATEL_RESERVATION_MEMORY=32M
export TEST_01_RESERVATION_MEMORY=512M
export STAGING_01_RESERVATION_MEMORY=512M
export CONTROLLER_RESERVATION_MEMORY=512M
export EM_RESERVATION_MEMORY=512M
export LINKER_RESERVATION_MEMORY=512M
export API_RESERVATION_MEMORY=512M


# export DOCKER_MEM_G=28
# export DOCKER_MEM_MB=(${DOCKER_MEM_G}*1024)

# UNIT_ZOOKEEPER=2        #    2%
# UNIT_KAFKA=8            #   24% 8 * 3
# UNIT_MYSQL=8            #    8% 
# UNIT_ZERO=3             #    3%
# UNIT_ALPHA=10           #   30% 10 * 3
# UNIT_RATEL=1            #    1%
# UNIT_TESTER=3           #    3%
# UNIT_STAGING=3          #    3%
# UNIT_CONTROLLER=6       #    6%
# UNIT_EM=10              #   10%
# UNIT_LINKER=10          #   10%
#                         # -----
#                         #  100%
#                         # =====

# MEM_UNITS=`echo "(${UNIT_ZOOKEEPER}+${UNIT_KAFKA}*3+${UNIT_ZERO}+${UNIT_ALPHA}*3+${UNIT_RATEL}+${UNIT_MYSQL}+${UNIT_TESTER}+${UNIT_STAGING}+${UNIT_CONTROLLER}+${UNIT_EM}+${UNIT_LINKER})" | bc`
# ZOOKEEPER_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_ZOOKEEPER}/${MEM_UNITS})" | bc -l`
# KAFKA_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_KAFKA}/${MEM_UNITS})" | bc -l`
# MYSQL_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_MYSQL}/${MEM_UNITS})" | bc -l`
# ZERO_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_ZERO}/${MEM_UNITS})" | bc -l`
# ALPHA_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_ALPHA}/${MEM_UNITS})" | bc -l`
# RATEL_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_RATEL}/${MEM_UNITS})" | bc -l`
# TESTER_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_TESTER}/${MEM_UNITS})" | bc -l`
# STAGING_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_STAGING}/${MEM_UNITS})" | bc -l`
# CONTROLLER_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_CONTROLLER}/${MEM_UNITS})" | bc -l`
# EM_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_EM}/${MEM_UNITS})" | bc -l`
# LINKER_MEMORY=`echo "${DOCKER_MEM_MB}*(${UNIT_LINKER}/${MEM_UNITS})" | bc -l`

# export ZOOKEEPER_LIMIT_MEMORY=`echo $ZOOKEEPER_MEMORY | xargs printf '%.0f'`
# export ZOOKEEPER_RESERVATION_MEMORY=`echo "$ZOOKEEPER_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export KAFKA_LIMIT_MEMORY=`echo $KAFKA_MEMORY | xargs printf '%.0f'`
# export KAFKA_RESERVATION_MEMORY=`echo "$KAFKA_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export MYSQL_LIMIT_MEMORY=`echo $MYSQL_MEMORY | xargs printf '%.0f'`
# export MYSQL_RESERVATION_MEMORY=`echo "$MYSQL_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export ZERO_LIMIT_MEMORY=`echo $ZERO_MEMORY | xargs printf '%.0f'`
# export ZERO_RESERVATION_MEMORY=`echo "$ZERO_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export ALPHA_LIMIT_MEMORY=`echo $ALPHA_MEMORY | xargs printf '%.0f'`
# export ALPHA_RESERVATION_MEMORY=`echo "$ALPHA_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export RATEL_LIMIT_MEMORY=`echo $RATEL_MEMORY | xargs printf '%.0f'`
# export RATEL_RESERVATION_MEMORY=`echo "$RATEL_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export TESTER_LIMIT_MEMORY=`echo $TESTER_MEMORY | xargs printf '%.0f'`
# export TESTER_RESERVATION_MEMORY=`echo "$TESTER_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export STAGING_LIMIT_MEMORY=`echo $STAGING_MEMORY | xargs printf '%.0f'`
# export STAGING_RESERVATION_MEMORY=`echo "$STAGING_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export CONTROLLER_LIMIT_MEMORY=`echo $CONTROLLER_MEMORY | xargs printf '%.0f'`
# export CONTROLLER_RESERVATION_MEMORY=`echo "$CONTROLLER_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export EM_LIMIT_MEMORY=`echo $EM_MEMORY | xargs printf '%.0f'`
# export EM_RESERVATION_MEMORY=`echo "$EM_MEMORY/2" | bc -l | xargs printf '%.0f'`
# export LINKER_LIMIT_MEMORY=`echo $LINKER_MEMORY | xargs printf '%.0f'`
# export LINKER_RESERVATION_MEMORY=`echo "$LINKER_MEMORY/2" | bc -l | xargs printf '%.0f'`

# DON'T CHANGE
export REGISTRY_NODE_IP=localhost:5000/v2

envsubst < conf-env-1-pc.template > conf.env
