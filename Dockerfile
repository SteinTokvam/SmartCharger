# we will use openjdk 17 with oracle
FROM openjdk:17-oracle
MAINTAINER steintokvam
COPY target/smartcharger-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]