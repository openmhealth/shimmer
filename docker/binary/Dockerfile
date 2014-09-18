FROM openmhealth/openjdk-7-jre

MAINTAINER Emerson Farrugia <emerson@openmhealth.org>

RUN mkdir /root/omh-shims
ADD omh-shims-0.2.0.jar /root/omh-shims/shim-server.jar
EXPOSE 8083

CMD /usr/bin/java -jar /root/omh-shims/shim-server.jar
