FROM ubuntu:14.04
# installed packages are listed at http://packages.ubuntu.com/trusty/ubuntu-minimal

MAINTAINER Emerson Farrugia <emerson@openmhealth.org>

# update package lists
RUN apt-get update
RUN apt-get -y install software-properties-common

# install the Oracle Java 7 SDK
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update && apt-get -y upgrade
RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && apt-get -y install oracle-java7-installer
RUN apt-get clean && update-alternatives --display java
ENV JAVA_HOME /usr/lib/jvm/java-7-oracle

# install build tools
RUN apt-get -y install git maven curl

# install Mongo
# you can leave this section out if you prefer to use a separate container for MongoDB
RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
RUN echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | tee /etc/apt/sources.list.d/10gen.list
RUN apt-get update && apt-get install -y mongodb-org
RUN mkdir -p /data/db

# install OpenSSH
# you can leave this section out if you prefer to not install OpenSSH in the container and access the
# container using other means, e.g. http://blog.docker.com/2014/06/why-you-dont-need-to-run-sshd-in-docker/
RUN apt-get -y install openssh-server
RUN mkdir /var/run/sshd && echo 'root:docker' | chpasswd
RUN sed --in-place=.bak 's/without-password/yes/' /etc/ssh/sshd_config
EXPOSE 22

# install supervisord
# you can leave this section out if you've left out both Mongo and OpenSSH
RUN apt-get -y install supervisor
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# install the HealthVault Java library from https://healthvaultjavalib.codeplex.com/releases/view/125355
# this library is currently necessary even if you don't intend to integrate with HealthVault
RUN curl -o /root/healthvault-sdk-1.6.0.zip 'http://download-codeplex.sec.s-msft.com/Download/Release?ProjectName=healthvaultjavalib&DownloadId=878881&FileTime=130496923011230000&Build=20928'
RUN cd /root && unzip healthvault-sdk-1.6.0.zip && cd healthvault-sdk-1.6.0 && mvn install -N && mvn install --pl sdk,hv-jaxb -DskipTests

# clone the repository and build it
RUN cd /root && git clone https://github.com/openmhealth/omh-shims.git && cd /root/omh-shims && mvn compile
EXPOSE 8083

CMD ["/usr/bin/supervisord"]

# if you're not using supervisord, replace the above with
# CMD ["cd root/omh-shims && mvn spring-boot:run"]