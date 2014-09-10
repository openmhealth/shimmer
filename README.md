## Open mHealth shims and shim server

### Overview

A *shim* is an adapter that reads raw health data from a third-party API and converts 
that data into an [Open mHealth compliant data format](http://www.openmhealth.org/developers/schemas/). It's called a shim
because it lets you treat the third-party API like an Open mHealth compliant endpoint when writing your application. 
To learn more about shims, please visit the [shim section](http://www.openmhealth.org/developers/apis/) on the main site.
 
A shim is a library, not an application. To use a shim, it needs to be hosted in a standalone application called a *shim server*. 
The API exposed by the shim server lets your application use a shim to read data in either the raw format produced by the third-party API or in a 
converted Open mHealth compliant format. To choose the shims you want to enable in the shim server, please follow the instructions below.
 
This repository contains a shim server and shims for third-party APIs. The currently supported APIs are:

* [Fat Secret](http://platform.fatsecret.com/api/)
* [Fitbit](http://dev.fitbit.com/)
* [Microsoft HealthVault](https://developer.healthvault.com/)
* [Jawbone UP](https://jawbone.com/up/developer)
* [RunKeeper](http://developer.runkeeper.com/healthgraph)
    * [application management portal](http://runkeeper.com/partner)
* [Withings](http://oauth.withings.com/api)

The above links point to the developer website of each API. You'll need to visit these websites to register your 
application and obtain authentication credentials for each of the shims you want to enable.  

If any of links are incorrect or out of date, please [submit an issue](https://github.com/openmhealth/omh-shims/issues) to let us know. 
  

### Installation

There are two ways to install and run the shim server. You can either run it in a Docker container, or you can install and
run it manually.

### Docker installation

If you don't have Docker installed, download [Docker](https://docs.docker.com/installation/#installation/) 
 and follow the installation instructions for your platform.
 
Then

1. Download the [docker](https://github.com/openmhealth/omh-shims/docker) directory.
1. Navigate to the directory in a terminal.
1. Run `docker build -t="openmhealth/omh-shim-server" .`
1. Run `docker run -d -p 8083:8083 -p 2022:22 openmhealth/omh-shim-server`. 
1. The server should now be running on the default port 8083. You can change the port number in the Docker `run` command.

If you want to SSH into the container, run `ssh root@<your-docker-host> -p 2022`. The password is `docker`.

### Manual installation

If you prefer not to use Docker,  

1. You must have a [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html/) or higher JDK installed. 
1. A running [MongoDB](http://http://docs.mongodb.org/manual/) installation is required.
1. [Gradle](http://www.gradle.org/) or [Maven](http://maven.apache.org/) is required to build the source code.  
1. [Maven](http://maven.apache.org/) is required to build and install Microsoft HealthVault libraries.  

Then

1. Clone this Git repository.
1. Navigate to the `src/main/resources` directory and edit the `application.yaml` file.
1. Check that the `spring:data:mongodb:uri` parameter points to your running MongoDB instance.
1. Follow [these instructions](#preparing-to-use-microsoft-healthvault) to install Microsoft HealthVault libraries. These libraries are
 currently required for the shim server to work.
1. To build and run the shim server, navigate to the project directory in a terminal. 
  * If you're using Maven, run `mvn spring-boot:run`
  * If using Gradle, run `gradle bootRun`
1. The server should now be running on the default port 8083. You can change the port number in the `application.yaml` file.

#### Preparing to use Microsoft HealthVault
    
The Microsoft HealthVault shim has dependencies which can't be automatically downloaded from public servers, at least 
not yet. To add HealthVault support to the shim server,

1. Download the HealthVault Java Library version [R1.6.0](https://healthvaultjavalib.codeplex.com/releases/view/125355) archive.
1. Extract the archive.
1. Navigate to the extracted directory in a terminal.
1. Run `mvn install`
  
This will make the HealthVault libraries available to both Maven and Gradle.  

### Setting up your credentials

You need to obtain authentication credentials, typically an OAuth client ID and client secret, for each shim you'd like to run. 
These are obtained from the developer websites of the third-party APIs.

Once obtained, uncomment and replace the corresponding `clientId` and `clientSecret` placeholders in the `application.yaml` file 
with your new credentials and restart Jetty. 

If you installed using Docker, you can restart Jetty using `supervisord restart jetty`. If you installed manually,
terminate your running Gradle or Maven process and restart it.

### Authorizing access to a third-party user account

The data produced by a third-party API belongs to some user account registered on the third-party system. To allow 
 a shim read that data, you'll need to initiate an authorization process that lets the account holder grant the shim access to their data.

To initiate the authorization process, do the following:
 
1. Go to the URL `http://localhost:8083/authorize/{shim}?username={userId}` in a browser.
  * The `shim` path parameter should be one of the names listed [below](#supported-apis-and-endpoints), e.g. `fitbit`. 
  * The `username` query parameter can be set to any unique identifier you'd like to use to identify the user. 
1. In the returned JSON response, find the `authorizationUrl` value and open this URL in a new browser window. 
You should be redirected to the third-party website where you can login and authorize access to your third-party user account. 
1. Once authorized, you should be redirected to `http://localhost:8083/authorize/{shim_name}/callback` and you'll see an approval response.

### Reading data
You can now pull data from the third-party API by making requests in the format
 
`http://localhost:8083/data/{shim}/{endPoint}?username={userId}&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize={true|false}`

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
      

