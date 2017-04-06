#!/bin/bash

cwd=$(pwd)
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${script_dir}

docker-compose -f local-dev-env.yml down

cd ${cwd}
exit 0
