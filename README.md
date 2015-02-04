## Open mHealth shims and shim server

### Overview

A *shim* is an adapter that reads raw data from a third-party API (e.g. Jawbone, Fitbit) and converts that data into an [Open mHealth compliant data format](http://www.openmhealth.org/developers/schemas/). It's called a shim
because it lets you treat third-party data like Open mHealth compliant data when writing your application. 
To learn more about shims, please visit the [shim section](http://www.openmhealth.org/developers/apis/) on our site.
 
A shim is a library, not an application. To use a shim, it needs to be hosted in a standalone application called a *shim server*. 
The shim server API lets your application use a shim to read data from a third-party API. This data is available in two formats;
 the raw format produced by the third-party API and the converted Open mHealth compliant format. To make it easier to use the shim
 server, we've provided a *shim server UI* that can trigger authentication flows and make requests.
 
This repository contains a shim server, a shim server UI, and shims for third-party APIs. The currently supported APIs are:

* [Fat Secret](http://platform.fatsecret.com/api/)
* [Fitbit](http://dev.fitbit.com/)
* [Microsoft HealthVault](https://developer.healthvault.com/)
* [Jawbone UP](https://jawbone.com/up/developer)
* [RunKeeper](http://developer.runkeeper.com/healthgraph) ([application management portal](http://runkeeper.com/partner))
* [Withings](http://oauth.withings.com/api)

The above links point to the developer website of each API. You'll need to visit these websites to register your 
application and obtain authentication credentials for each of the shims you want to enable.  

If any of links are incorrect or out of date, please [submit an issue](https://github.com/openmhealth/omh-shims/issues) to let us know. 

Please note that the shim server is meant as a discovery and experimentation tool. It has not been secured and does not
attempt to protect the data retrieved from third-party APIs.


### Installation

There are three ways to build and run the shim server. 

1. You can run a Docker container that executes a pre-built binary. 
  * This is the fastest way to get up and running and isolates the install from your system.
1. You can build all the code from source and run it on your host system.
  * This is a quick way to get up and running
   if your system already has MongoDB and is prepped to build Java code. 
1. You can run a Docker container that builds all the code from source and executes the resulting binary.
  * This isolates the install from your system while still letting you hack the code. But it can take a while to build
   the container due to the large number of libraries and subsystems that need to be downloaded and installed.
  * If you know Docker and want to speed things up, remove optional components from the Dockerfile and link them
  from separate containers.

#### Option 1. Running a pre-built binary in Docker

If you don't have Docker installed, download [Docker](https://docs.docker.com/installation/#installation/) 
 and follow the installation instructions for your platform.

Then in a terminal,

1. If you don't already have a MongoDB container, download one by running
  * `docker pull mongo:latest`
  * Note that this will download around 400 MB of Docker images.
1. If your MongoDB container isn't running, start it by running
  * `docker run --name some-mongo -d mongo:latest`
1. Download the shim server image by running
  * `docker pull openmhealth/omh-shim-server:latest` 
  * Note that this will download up to 600MB of Docker images. (203MB for Ubuntu, 350MB for the OpenJDK 7 JRE, and 30MB 
    for the shim server and its dependencies.)
1. Start the shim server by running
  * `docker run -e openmhealth.shim.server.callbackUrlBase=http://<your-docker-host>:8083 --link some-mongo:mongo -d -p 8083:8083 'openmhealth/omh-shim-server:latest'`
1. The server should now be running on the Docker host on default port 8083. You can change the port number in the Docker `run` command.
1. Visit `http://<your-docker-host>:8083` in a browser.

#### Option 2. Building from source and running on your host system

If you prefer not to use Docker,  

1. You must have a Java 7 or higher JDK installed. You can use either [OpenJDK](http://openjdk.java.net/install/) or the [Oracle JDK](http://www.oracle.com/technetwork/java/javase/downloads/index.html).
1. A running [MongoDB](http://docs.mongodb.org/manual/) installation is required.
1. [Gradle](http://www.gradle.org/) or [Maven](http://maven.apache.org/) is required to build the source code.  
1. [Maven](http://maven.apache.org/) is required to build and install Microsoft HealthVault libraries.
1. You technically don't need to run the shim server UI, but it makes your life easier. If you're building the UI,
  1. [Node.js](http://nodejs.org/download/) is required.
  1. [Xcode Command Line Tools](https://developer.apple.com/xcode/) are required if you're on a Mac.

Then,

1. Clone this Git repository.
1. Navigate to the `shim-server-ui` directory in a terminal and run
  1. `npm install`
  1. `sudo npm install -g grunt-cli bower`
  1. `bower install`
  1. `grunt build`
  1. `ln -s dist ../shim-server/src/main/resources/public`
1. Navigate to the `shim-server/src/main/resources` directory and edit the `application.yaml` file.
1. Check that the `spring:data:mongodb:uri` parameter points to your running MongoDB instance.
  * You might need to change the host to `localhost`, for example.
1. Follow [these instructions](#preparing-to-use-microsoft-healthvault) to install Microsoft HealthVault libraries. These libraries are
 currently required for the shim server to work.
1. To build and run the shim server, navigate to the `shim-server` directory and 
  * If you're using Maven, run `mvn spring-boot:run`
  * If using Gradle, run `gradle bootRun`
1. The server should now be running on `localhost` on port 8083. You can change the port number in the `application.yaml` file.
1. Visit `http://localhost:8083` in a browser.
                           
##### Preparing to use Microsoft HealthVault
    
The Microsoft HealthVault shim has dependencies which can't be automatically downloaded from public servers, at least 
not yet. To add HealthVault support to the shim server,

1. Download the HealthVault Java Library version [R1.6.0](https://healthvaultjavalib.codeplex.com/releases/view/125355) archive.
1. Extract the archive.
1. Navigate to the extracted directory in a terminal.
1. Run `mvn install -N && mvn install --pl sdk,hv-jaxb -DskipTests`
  
This will make the HealthVault libraries available to both Maven and Gradle.  

#### Option 3. Building from source and running in Docker

If you don't have Docker installed, download [Docker](https://docs.docker.com/installation/#installation/) 
 and follow the installation instructions for your platform.

Then,

1. Download the latest [release](https://github.com/openmhealth/omh-shims/releases) of this Git repository or clone it. 
1. Navigate to the `docker/source` directory in a terminal.
1. Run `docker build -t="openmhealth/omh-shim-server" .`
  * This will require about 1.5GB of disk space.  
1. Run `docker run -d -p 8083:8083 -p 2022:22 openmhealth/omh-shim-server`
1. The server should now be running on the Docker host on default port 8083. You can change the port number in the Docker `run` command.
1. Visit `http://<your-docker-host>:8083` in a browser.

If you want to SSH into the container, run `ssh root@<your-docker-host> -p 2022`. The password is `docker`.

### Setting up your credentials

You need to obtain authentication credentials, typically an OAuth client ID and client secret, for any shim you'd like to run. 
These are obtained from the developer websites of the third-party APIs.

Once credentials are obtained for a particular API, navigate to the settings tab of the shim server UI and fill them in. 

(If you didn't build the UI, uncomment and replace the corresponding `clientId` and `clientSecret` placeholders in the `application.yaml` file 
with your new credentials and restart Jetty. If you installed using Docker, you can restart Jetty using `supervisorctl restart jetty`. 
If you installed manually, terminate your running Gradle or Maven process and restart it.)

### Authorising access to a third-party user account from the UI

The data produced by a third-party API belongs to some user account registered on the third-party system. To allow 
 a shim to read that data, you'll need to initiate an authorization process that lets the account holder grant the shim access to their data.

To initiate the authorization process from the UI,
 
1. Type in an arbitrary user handle. This handle can be anything, it's just your way of referring to third-party API users. 
1. Press *Find* and the UI will show you a *Connect* button for each API whose authentication credentials have been [configured](#setting-up-your-credentials).
1. Click *Connect* and a pop-up will open.
1. Follow the authorization prompts. You should see an `AUTHORIZE` JSON response.
1. Close the pop-up.

### Authorising access to a third-party user account programmatically

To initiate the authorization process programmatically,
 
1. Make a GET request to `http://<host>:8083/authorize/{shim}?username={userId}`
  * The `shim` path parameter should be one of the names listed [below](#supported-apis-and-endpoints), e.g. `fitbit`. 
  * The `username` query parameter can be set to any unique identifier you'd like to use to identify the user. 
1. In the returned JSON response, find the `authorizationUrl` value and redirect your user to this URL. Your user will land on the third-party website where they can login and authorize access to their third-party user account. 
1. Once authorized, they will be redirected to `http://<host>:8083/authorize/{shim_name}/callback` along with an approval response.

### Reading data using the UI

To pull data from the third-party API using the UI,
 
1. Click the nam of the connected third-party API.
1. Fill in the date range you're interested in.
1. Press the *Raw* button for raw data, or the *Normalized* button for data that has been converted to an Open mHealth compliant data format. 

### Reading data programmatically

To pull data from the third-party API programmatically, make requests in the format
 
`http://<host>:8083/data/{shim}/{endPoint}?username={userId}&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize={true|false}`

The URL can be broken down as follows
* The `shim` and `username` path variables are the same as [above](#authorizing-access-to-a-third-party-user-account).
* The `endPoint` path variable roughly corresponds to the type of data to retrieve. There's a list of these [below](#supported-apis-and-endpoints).
* The `normalize` parameter controls whether the shim returns data in a raw third-party API format (`false`) or in an Open mHealth compliant format (`true`).  
 
### Supported APIs and endpoints

The following is a nested list in the format  

* shim name
   * endpoint name
      * Open mHealth compliant data the endpoint can produce

The currently supported shims are
 
* fitbit
    * activity
        * [omh:physical-activity](http://www.openmhealth.org/developers/schemas/#physical-activity)
    * blood_pressure
        * [omh:blood-pressure](http://www.openmhealth.org/developers/schemas/#blood-pressure)
    * blood_glucose
        * [omh:blood-glucose](http://www.openmhealth.org/developers/schemas/#blood-glucose)
    * heart
        * [omh:heart-rate](http://www.openmhealth.org/developers/schemas/#heart-rate)
    * steps
        * [omh:step-count](http://www.openmhealth.org/developers/schemas/#step-count)
    * weight
        * [omh:body-weight](http://www.openmhealth.org/developers/schemas/#body-weight)
* healthvault
    * activity 
        * [omh:physical-activity](http://www.openmhealth.org/developers/schemas/#physical-activity)
    * blood_glucose
        * [omh:blood-glucose](http://www.openmhealth.org/developers/schemas/#blood-glucose)
    * blood_pressure
        * [omh:blood-pressure](http://www.openmhealth.org/developers/schemas/#blood-pressure)
        * [omh:heart-rate](http://www.openmhealth.org/developers/schemas/#heart-rate)
    * height
        * [omh:body-height](http://www.openmhealth.org/developers/schemas/#body-height)
    * weight
        * [omh:body-weight](http://www.openmhealth.org/developers/schemas/#body-weight)
* jawbone
    * body
        * [omh:body-weight](http://www.openmhealth.org/developers/schemas/#body-weight)
    * moves
        * [omh:step-count](http://www.openmhealth.org/developers/schemas/#step-count)
    * sleep
        * [omh:sleep-duration](http://www.openmhealth.org/developers/schemas/#sleep-duration)
    * workouts
        * [omh:physical-activity](http://www.openmhealth.org/developers/schemas/#physical-activity)
* runkeeper
    * activity
        * [omh:physical-activity](http://www.openmhealth.org/developers/schemas/#physical-activity)
    * weight  
        * [omh:body-weight](http://www.openmhealth.org/developers/schemas/#body-weight)
* withings
    * body 
        * [omh:blood-pressure](http://www.openmhealth.org/developers/schemas/#blood-pressure)
        * [omh:body-height](http://www.openmhealth.org/developers/schemas/#body-height)
        * [omh:body-weight](http://www.openmhealth.org/developers/schemas/#body-weight)
        * [omh:heart-rate](http://www.openmhealth.org/developers/schemas/#heart-rate)
    * intraday
        * [omh:step-count](http://www.openmhealth.org/developers/schemas/#step-count)
    * sleep    
        * [omh:sleep-duration](http://www.openmhealth.org/developers/schemas/#sleep-duration)

You can learn more about these shims and endpoints on the Open mHealth [developer site](http://www.openmhealth.org/developers/getting-started/). 

The list of supported third-party APIs will grow over time as more shims are added. If you'd like to contribute a shim to work with your API or a third-party API,
send us a [pull request](https://github.com/openmhealth/omh-shims/pulls). If you need any help, feel free to
reach out on [admin@openmhealth.org](mailto://admin@openmhealth.org) or on the Open mHealth [developer group](https://groups.google.com/forum/#!forum/omh-developers).
      

