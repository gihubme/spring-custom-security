FROM openjdk:11-jre-slim-buster

# Install prerequisites ..not tested
RUN apt-get update && apt-get install -y \
curl

# Refer to Maven build -> finalName
ENV APP_HOME=target/*.jar

# cd /opt/app
#WORKDIR /opt/app

# cp target/spring-boot-web.jar /opt/app/app.jar
COPY ${APP_HOME} app.jar

# java -jar /opt/app/app.jar
ENTRYPOINT ["java","-jar","app.jar"]
