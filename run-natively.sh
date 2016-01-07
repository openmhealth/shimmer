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

# build the console
echo -n "Do you want to rebuild the console (y/N)? "
read answer
if echo "$answer" | grep -iq "^y" ;then
    cd ${BASEDIR}/shim-server-ui #CMD

    if ! isNpmPackageInstalled grunt-cli
    then
        echo Installing Grunt, you may be asked for your password to run sudo...
        sudo npm install -g grunt-cli #CMD
    else
        echo Grunt is already installed, skipping...
    fi

    if ! isNpmPackageInstalled bower
    then
        echo Installing Bower, you may be asked for your password to run sudo...
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

    cd ${BASEDIR}/shim-server/src/main/resources #CMD
    ln -sfh ../../../../shim-server-ui/docker/assets public
    #CMD create a symlink called shim-server/src/main/resources/public to the Grunt output directory
fi

echo "The MongoDB hostname defaults to the setting in application.yaml. Initially the host name is 'mongo'."
echo -n "Please enter a hostname to override it, or press Enter to keep the default? "
read answer
trimmed=${answer// /}

# start the resource server
echo Starting the resource server...
cd ${BASEDIR}
if [[ ! -z "$trimmed" ]] ;then
    SPRING_DATA_MONGODB_URI="mongodb://${trimmed}:27017/omh_dsu" ./gradlew shim-server:bootRun #CMD
    else
    ./gradlew shim-server:bootRun #CMD
fi


