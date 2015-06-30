FROM ubuntu:14.04
# installed packages are listed at http://packages.ubuntu.com/trusty/ubuntu-minimal

MAINTAINER Jared Sieling <jared.sieling@gmail.com>

# update package lists
RUN apt-get update && apt-get -y upgrade

# install build tools
RUN apt-get -y install git maven curl nodejs nodejs-legacy npm openjdk-7-jdk && apt-get clean
RUN update-alternatives --display java

# install Mongo
# you can leave this section out and update supervisord.conf if you prefer to use a separate container for MongoDB
# RUN apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 7F0CEB10
# RUN echo 'deb http://downloads-distro.mongodb.org/repo/ubuntu-upstart dist 10gen' | tee /etc/apt/sources.list.d/10gen.list
# RUN apt-get update && apt-get install -y mongodb-org
# RUN mkdir -p /data/db

# install OpenSSH
# you can leave this section out and update supervisord.conf if you prefer to not install OpenSSH in the container and
# access the container using other means, e.g. http://blog.docker.com/2014/06/why-you-dont-need-to-run-sshd-in-docker/
# RUN apt-get -y install openssh-server
# RUN mkdir /var/run/sshd && echo 'root:docker' | chpasswd
# RUN sed --in-place=.bak 's/without-password/yes/' /etc/ssh/sshd_config
# EXPOSE 22

# install supervisord
# you can leave this section out if you've left out both Mongo and OpenSSH
# RUN apt-get -y install supervisor
# COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

# install the HealthVault Java library from https://healthvaultjavalib.codeplex.com/releases/view/125355
# this library is currently necessary even if you don't intend to integrate with HealthVault
RUN curl -o /root/healthvault-sdk-1.6.0.zip 'http://download-codeplex.sec.s-msft.com/Download/Release?ProjectName=healthvaultjavalib&DownloadId=878881&FileTime=130496923011230000&Build=21018'
WORKDIR /root
RUN unzip healthvault-sdk-1.6.0.zip && cd healthvault-sdk-1.6.0 && mvn install -N && mvn install --pl sdk,hv-jaxb -DskipTests

# clone the repository
WORKDIR /root
RUN git clone https://github.com/smalldatalab/omh-shims.git
WORKDIR /root/omh-shims
RUN git checkout master

# build the frontend
WORKDIR /root/omh-shims/shim-server-ui
RUN npm install -g grunt-cli bower
RUN npm install
RUN bower install --allow-root
RUN grunt build

# build the backend
WORKDIR /root/omh-shims/shim-server
RUN ln -s dist ../shim-server/src/main/resources/public && mvn compile
EXPOSE 8083

# CMD ["/usr/bin/supervisord"]

# if you're not using supervisord, replace the above with
CMD cd root/omh-shims && mvn spring-boot:run
