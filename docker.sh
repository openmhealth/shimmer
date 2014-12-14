#!/bin/sh

BASEDIR=`pwd`
BUILD_VERSION='0.2.2'

docker build -t "openmhealth/omh-shim-server:$BUILD_VERSION" docker/binary
docker build -t "openmhealth/omh-shim-server:latest" docker/binary
