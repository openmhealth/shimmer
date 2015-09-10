#!/bin/bash

BASEDIR=`pwd`

isNpmPackageInstalled() {
  npm list --depth 1 -g $1 > /dev/null 2>&1
}

# check dependencies
if ! hash "npm" 2>/dev/null;
then
    echo "npm can't be found"
    exit 1
fi

if ! hash "docker-machine" 2>/dev/null;
then
    echo "docker-machine can't be found"
    exit 1
fi

if [[ -z "$DOCKER_MACHINE_NAME" ]]; then
    echo "DOCKER_MACHINE_NAME environment variable isn't set. Have you run docker-machine env?"
    exit 1
fi

if ! hash "docker-compose" 2>/dev/null;
then
    echo "docker-compose can't be found"
    exit 1
fi

# build the console
echo -n "Do you want to rebuild the console (y/N)? "
read answer
if echo "$answer" | grep -iq "^y" ;then
    cd ${BASEDIR}/shim-server-ui #CMD

    if ! isNpmPackageInstalled grunt-cli
    then
        echo Installing Grunt. You may be asked for your password to run sudo...
        sudo npm install -g grunt-cli #CMD
    else
        echo Grunt is already installed, skipping...
    fi

    if ! isNpmPackageInstalled bower
    then
        echo Installing Bower. You may be asked for your password to run sudo...
        sudo npm install -g bower #CMD
    else
        echo Bower is already installed, skipping...
    fi

    echo Installing npm dependencies...
    npm install #CMD

    echo Installing Bower dependencies...
    bower install #CMD

    echo Building the console...
    grunt build #CMD
fi

# build the backend
echo -n "Do you want to rebuild the API endpoint (y/N)? "
read answer
if echo "$answer" | grep -iq "^y" ;then
    echo Building the API endpoint...
    cd ${BASEDIR} #CMD
    ./gradlew build #CMD
fi

# run the containers
ip=$(docker-machine ip $DOCKER_MACHINE_NAME)
echo Updating the Docker Compose configuration to match your environment...
cd ${BASEDIR} #CMD
sed -i ".bak" -e "s,.*OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE.*,    OPENMHEALTH_SHIM_SERVER_CALLBACKURLBASE: http://${ip}:8083,g" docker-compose-build.yml
#CMD modify the URL in docker-compose-build.yml to point to the IP of your Docker host

echo Building the containers...
docker-compose -f docker-compose-build.yml build #CMD

echo Starting the containers in the background...
docker-compose -f docker-compose-build.yml up -d #CMD

echo Done

