FROM openjdk:11

# install cron
RUN apt-get update
RUN apt-get -y install cron
RUN rm -rf /var/lib/apt/lists/*

# copy in jars
COPY factorio-api-parser.jar factorio-api-parser.jar
COPY factorio-prototypes.jar factorio-prototypes.jar

# copy in go-executable
COPY webserver webserver


# copy in prototypes.json
COPY prototypes.json prototypes.json

# copy in startup script
COPY entry.sh entry.sh

# copy and enable the prototype conjob
COPY prototypeconjob prototypeconjob
RUN crontab prototypeconjob

# run startup script
ENTRYPOINT ./entry.sh
