FROM java:openjdk-8-jre
MAINTAINER Jared Sieling <jared.sieling@gmail.com>

ENV SERVER_PREFIX /opt/omh-shim

RUN mkdir -p $SERVER_PREFIX
ADD shim-server.jar $SERVER_PREFIX/
EXPOSE 8084

CMD /usr/bin/java -jar $SERVER_PREFIX/shim-server.jar --spring.config.location=file:$SERVER_PREFIX/