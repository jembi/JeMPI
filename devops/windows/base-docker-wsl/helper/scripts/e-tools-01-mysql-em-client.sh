#!/bin/bash

set -e 
set =u

pushd .
  SCRIPT_DIR=$(cd -P -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)
  cd ${SCRIPT_DIR}/../..

  source ./conf/images/conf-hub-images.sh

  docker run -it \
    --link JeMPI_mysql-em:mysql \
    --network backend-mysql \
    --rm $MYSQL_IMAGE sh -c 'exec mysql -h"mysql" -P"3306" -u"admin" -p"admin"'

popd    
