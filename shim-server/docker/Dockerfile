FROM java:openjdk-8-jre
MAINTAINER Emerson Farrugia <emerson@openmhealth.org>

ENV SERVER_PREFIX /opt/omh/shimmer

RUN mkdir -p $SERVER_PREFIX
ADD shimmer.jar $SERVER_PREFIX/
EXPOSE 8083

CMD /usr/bin/java -jar $SERVER_PREFIX/shimmer.jar --spring.config.location=file:$SERVER_PREFIX/
