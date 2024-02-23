#!/bin/bash

cd "$1"

for dir in */; do
  dir="${dir%/}" 
  if [ -f "$dir/pom.xml" ]; then
    echo "Running Checkstyle for $dir ..."
    mvn -f "$dir/pom.xml" checkstyle:check  -Dcheckstyle.suppressions.location="$dir/checkstyle/suppression.xml" 
  fi
done
