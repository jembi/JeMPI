#!/bin/bash

set -e
set -u

docker swarm init --advertise-addr $(hostname -i)

