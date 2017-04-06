#!/bin/bash

cwd=$(pwd)
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd ${script_dir}

docker-compose -f local-dev-env.yml up -d
containerId=$(docker-compose -f local-dev-env.yml ps -q)

i=0
while [ ${i} -lt 30 ]; do
    status=$(docker inspect --format='{{json .State.Status}}' $containerId)
    if [ "$status" == "\"running\"" ];
    then
        health=$(docker inspect --format='{{json .State.Health.Status}}' $containerId)
        if [ "$health" == "\"healthy\"" ];
        then
            echo "mariadb ready!"
            cd ${cwd}
            exit 0
        fi
    fi

    sleep 2
    ((i+=1))
done

docker-compose -f local-dev-env.yml down
echo "something went wrong!"
cd ${cwd}
exit 1
