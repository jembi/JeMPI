#!/bin/bash

set -e
set -u

FILE=test-data-0005000-02
DST_DIR=../../docker/docker_data/data-apps/async_receiver/csv

get_seeded_random()
{
  seed="$1"
  openssl enc -aes-256-ctr -pass pass:"$seed" -nosalt \
    </dev/zero 2>/dev/null
}

sed 1,1d ./results/$FILE.csv | shuf --random-source=<(get_seeded_random 42) >$DST_DIR/$FILE.temp
HEADER=$(head -1 ./results/$FILE.csv)
sed -i '1i '$HEADER $DST_DIR/$FILE.temp
rm -f $DST_DIR/$FILE.csv
mv $DST_DIR/$FILE.temp $DST_DIR/${FILE}.csv

