FROM openmhealth/openjdk-7-jre

MAINTAINER Emerson Farrugia <emerson@openmhealth.org>

RUN mkdir /root/omh-shims
ADD shim-server.jar /root/omh-shims/
EXPOSE 8083

CMD /usr/bin/java -jar /root/omh-shims/shim-server.jar
