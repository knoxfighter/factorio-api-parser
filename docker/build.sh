#!/bin/bash

# remove old docker tar
rm factorio-api.tar

# build jar
dockerDir="$PWD"

cd ../parser
./gradlew dockerJar -Dorg.gradle.java.home=/usr/lib/jvm/java-11-openjdk-amd64/ -Dme.test="$dockerDir"

# build go webserver
cd ../web/
go build -o "$dockerDir/webserver" .

# move back to this directory
cd "$dockerDir"

# build docker image
docker build . -t factorio-api

# save docker image into file
docker save -o factorio-api.tar factorio-api

# cleanup again
rm webserver
rm factorio-api-parser.jar
