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

# remove the symlink which may have been created by running natively earlier
rm -f ${BASEDIR}/shim-server/src/main/resources/public #CMD

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
echo -n "Do you want to rebuild the resource server (y/N)? "
read answer
if echo "$answer" | grep -iq "^y" ;then
    echo Building the resource server...
    cd ${BASEDIR} #CMD
    ./gradlew build #CMD
fi

# run the containers
cd ${BASEDIR} #CMD
echo Building the containers...
docker-compose -f docker-compose-build.yml build #CMD

echo Starting the containers in the background...
docker-compose -f docker-compose-build.yml up -d #CMD

echo Done, containers are starting up and may take up to a minute to be ready.

