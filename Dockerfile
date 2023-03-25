# we will use openjdk 17 with oracle
FROM openjdk:17-oracle
ARG PROFILE
ENV PROFILE_VAR=$PROFILE
VOLUME /tmp
## Add the built jar for docker image building
ADD target/smartcharger.jar smartcharger.jar
ENTRYPOINT ["/bin/bash", "-c", "java","-Dspring.profiles.active=$PROFILE_VAR","-jar","/smartcharger.jar"]
## DO NOT USE(The variable would not be substituted): ENTRYPOINT ["java","-Dspring.profiles.active=$PROFILE_VAR","-jar","/hello-world-docker-action.jar"]
## CAN ALSO USE: ENTRYPOINT java -Dspring.profiles.active=$PROFILE_VAR -jar /hello-world-docker-action.jar