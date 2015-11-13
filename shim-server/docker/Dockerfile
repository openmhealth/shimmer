FROM java:openjdk-8-jre
MAINTAINER Jared Sieling <jared.sieling@gmail.com>

ENV SERVER_PREFIX /opt/omh/shimmer

RUN mkdir -p $SERVER_PREFIX
ADD shimmer.jar $SERVER_PREFIX/
EXPOSE 8084

CMD /usr/bin/java -jar $SERVER_PREFIX/shimmer.jar --spring.config.location=file:$SERVER_PREFIX/
