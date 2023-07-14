#!/bin/bash

set -e
set -u

source ./helper/java/start-app-async-receiver.sh &
source ./helper/java/start-app-etl.sh &
source ./helper/java/start-app-controller.sh