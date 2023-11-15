#!/bin/bash

set -e
set -u

mvn versions:display-dependency-updates "-Dmaven.version.ignore=.*-M.*,.*-alpha.*,.*-rc1"
