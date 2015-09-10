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

if ! hash "docker-compose" 2>/dev/null;
then
    echo "docker-compose can't be found"
    exit 1
fi

# check for Docker Compose tooling and update configuration
. update-compose-files.sh

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
echo Building the containers...
docker-compose -f docker-compose-build.yml build #CMD

echo Starting the containers in the background...
docker-compose -f docker-compose-build.yml up -d #CMD

echo Done

