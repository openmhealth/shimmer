#!/bin/bash

BASEDIR=`pwd`

isNpmPackageInstalled() {
  npm list --depth 1 -g $1 > /dev/null 2>&1
}

# check dependencies
if [[ -x "npm" ]]; then
    echo "npm can't be found"
    exit 1
fi

echo -n "Do you want to build the console (y/n)? "
read answer
if echo "$answer" | grep -iq "^y" ;then
    cd ${BASEDIR}/shim-server-ui

    if ! isNpmPackageInstalled grunt-cli
    then
        echo Installing Grunt, you may be asked for your password to run sudo...
        sudo npm install -g grunt-cli
    else
        echo Grunt is already installed, skipping...
    fi

    if ! isNpmPackageInstalled bower
    then
        echo Installing Bower, you may be asked for your password to run sudo...
        sudo npm install -g bower
    else
        echo Bower is already installed, skipping...
    fi

    echo Installing npm dependencies...
    npm install

    echo Installing Bower dependencies...
    bower install

    echo Building console...
    grunt build

    cd ${BASEDIR}/shim-server/src/main/resources
    ln -sfh ../../../../shim-server-ui/docker/assets public
fi

echo -n "Please enter your MongoDB hostname (defaults to mongo)? "
read answer
trimmed=${answer// /}
if [[ ! -z "$trimmed" ]] ;then
    cd ${BASEDIR}/shim-server/src/main/resources
    sed -i ".bak" -e "s,.*uri: mongodb.*,      uri: mongodb://${answer}:27017/omh_dsu,g" application.yaml
fi

echo Starting the Shimmer API endpoint
cd ${BASEDIR}
./gradlew shim-server:bootRun


