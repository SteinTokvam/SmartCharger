# we will use openjdk 17 with oracle
FROM openjdk:17-oracle
ARG PROFILE
ENV PROFILE_VAR=$PROFILE
VOLUME /tmp
## Add the built jar for docker image building
ADD target/smartcharger.jar smartcharger.jar

## Build a shell script because the ENTRYPOINT command doesn't like using ENV
RUN echo "#!/bin/bash \n java -Dspring.profiles.active=${PROFILE_VAR} -jar /smartcharger.jar" > ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

## Run the generated shell script.
ENTRYPOINT ["./entrypoint.sh"]
