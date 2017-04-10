#!/bin/bash

echo "TRAVIS_TAG: $TRAVIS_TAG"

if [ ! -z "$TRAVIS_TAG" ]; then
    echo "This will push to docker"
    openssl aes-256-cbc -K $encrypted_bdbf36a38ce4_key -iv $encrypted_bdbf36a38ce4_iv -in secrets.zip.enc -out secrets.zip -d
    unzip secrets.zip
    mv application-production.properties src/main/resources/application-production.properties
    mv application-staging.properties src/main/resources/application-staging.properties
    docker login -e $DOCKER_EMAIL -u $DOCKER_USER -p $DOCKER_PASS $DOCKER_URL
    docker build -f Dockerfile -t $DOCKER_REPO .
    docker tag $DOCKER_REPO $DOCKER_URL/$DOCKER_REPO:$TRAVIS_TAG
    docker push $DOCKER_URL/$DOCKER_REPO:$TRAVIS_TAG
    rm "/home/${USER}/.docker/config.json"

    if [[ $TRAVIS_TAG == *stage ]]; then
        echo "This will deploy to stage"
        chmod 600 id_rsa_breakout_deploy
        eval "$(ssh-agent -s)"
        ssh-add id_rsa_breakout_deploy
        ssh -o StrictHostKeyChecking=no $STAGE_LOGIN "docker-compose stop;"
        scp -o StrictHostKeyChecking=no "docker-compose.yml" $STAGE_LOGIN
        ssh -o StrictHostKeyChecking=no $STAGE_LOGIN "BACKEND_VERSION=$TRAVIS_TAG BACKEND_PROFILE=staging FRONTEND_VERSION=latest docker-compose pull"
        ssh -o StrictHostKeyChecking=no $STAGE_LOGIN "BACKEND_VERSION=$TRAVIS_TAG BACKEND_PROFILE=staging FRONTEND_VERSION=latest docker-compose up -d"
        ssh -o StrictHostKeyChecking=no $STAGE_LOGIN "docker restart backend"
    else
        echo "This will not deploy to stage, this is not with stage tag"
    fi

else
    echo "This will not be pushed to docker, there is no tag"
fi
