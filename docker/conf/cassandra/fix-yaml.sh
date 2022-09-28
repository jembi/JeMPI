#!/bin/bash

set -e 
set -u

sed -i 's/enable_sasi_indexes: false/enable_sasi_indexes: true/g' cassandra-1.yaml
sed -i 's/enable_materialized_views: false/enable_materialized_views: true/g' cassandra-1.yaml

sed -i 's/enable_sasi_indexes: false/enable_sasi_indexes: true/g' cassandra-2.yaml
sed -i 's/enable_materialized_views: false/enable_materialized_views: true/g' cassandra-2.yaml

sed -i 's/enable_sasi_indexes: false/enable_sasi_indexes: true/g' cassandra-3.yaml
sed -i 's/enable_materialized_views: false/enable_materialized_views: true/g' cassandra-3.yaml
