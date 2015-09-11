#!/bin/bash

BASEDIR=`pwd`

# check dependencies
if ! hash "docker-machine" 2>/dev/null;
then
    echo "docker-machine can't be found"
    exit 1
fi

if [[ -z "$DOCKER_MACHINE_NAME" ]]; then
    echo "DOCKER_MACHINE_NAME environment variable isn't set. Have you run docker-machine env?"
    exit 1
fi

# update the compose files
ip=$(docker-machine ip $DOCKER_MACHINE_NAME)
echo Updating the Docker Compose files to match your environment...
cd ${BASEDIR} #CMD
sed -i ".bak" -e "s,.*OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE.*,    OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE: http://${ip}:8083,g" docker-compose.yml
sed -i ".bak" -e "s,.*OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE.*,    OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE: http://${ip}:8083,g" docker-compose-build.yml
#CMD modify the URL in docker-compose.yml and docker-compose-build.yml to point to the IP of your Docker host

