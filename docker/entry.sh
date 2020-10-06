#!/bin/bash

# start cron
cron

# create directory for the parsed api
mkdir -p files

# run api parser
java -jar factorio-api-parser.jar ./files/

# run factorio prototypes for the first time
java -jar factorio-prototypes.jar ./files/

# run webserver
./webserver ./files/
