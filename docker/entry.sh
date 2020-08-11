#!/bin/bash

# create directory for the parsed api
mkdir -p files

# run api parser
java -jar factorio-api-parser.jar ./files/

# run webserver
./webserver ./files/
