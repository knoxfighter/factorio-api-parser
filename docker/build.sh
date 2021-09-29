#!/bin/bash

# remove old docker tar
rm factorio-api.tar

# build jar
dockerDir="$PWD"

# build parser
cd ../parser/lua-api
../gradlew jar -Dorg.gradle.java.home=/usr/lib/jvm/java-11-openjdk-amd64/ -Dme.test="$dockerDir"
# build wiki/prototype parser
cd ../prototype
../gradlew jar -Dorg.gradle.java.home=/usr/lib/jvm/java-11-openjdk-amd64/ -Dme.test="$dockerDir"
cd ..

# build go webserver
cd ../web/
go build -o "$dockerDir/webserver" .

# move back to this directory
cd "$dockerDir"

# copy over the prototypes files
cp ../files/prototypes/prototypes.json prototypes.json
cp ../files/prototypes/prototypes.lua prototypes.lua

# copy over README file
cp ../README.html README.html

# build docker image
docker build . -t factorio-api

# save docker image into file
docker save -o factorio-api.tar factorio-api

# cleanup again
rm webserver
rm factorio-api-parser.jar
rm factorio-prototypes.jar
