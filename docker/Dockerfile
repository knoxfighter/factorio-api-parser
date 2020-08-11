FROM openjdk:11

# copy in jar
COPY factorio-api-parser.jar factorio-api-parser.jar

# copy in go-executable
COPY webserver webserver

# copy in startup script
COPY entry.sh entry.sh

# run startup script
ENTRYPOINT entry.sh
