#!/bin/bash

BASEDIR=$(pwd)
TAG=$(git describe --exact-match)

if [[ ! ${TAG} =~ ^v[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
   echo "The tag ${TAG} isn't a valid version tag."
   exit
fi

VERSION=${TAG#v}

cd ${BASEDIR}/shim-server/docker
docker build -t "openmhealth/shimmer-resource-server:latest" .
docker build -t "openmhealth/shimmer-resource-server:${VERSION}" .
docker push "openmhealth/shimmer-resource-server:latest"
docker push "openmhealth/shimmer-resource-server:${VERSION}"

cd ${BASEDIR}/shim-server-ui/docker
docker build -t "openmhealth/shimmer-console:latest" .
docker build -t "openmhealth/shimmer-console:${VERSION}" .
docker push "openmhealth/shimmer-console:latest"
docker push "openmhealth/shimmer-console:${VERSION}"
