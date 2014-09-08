## Open mHealth shims and shim server

### Overview

A *shim* is an adapter that reads raw health data from a specific data source, typically a third-party API, and converts 
the data into an [Open mHealth compliant data format](http://www.openmhealth.org/developers/schemas/). It's called a shim
because it lets you treat a third-party API like any other Open mHealth compliant endpoint when writing your application.   
 
A shim is a library, not an application. To use shims, they need to be hosted in a standalone application called a *shim server*. 
The shim server lets your application make HTTP calls to read data in both the raw format produced by the third-party API, and in the 
converted Open mHealth compliant format. To choose the shims you want to enable in the shim server, please follow the instructions below.
 
This repository contains a shim server and shims for third-party APIs. The currently supported APIs are:

* [RunKeeper](http://developer.runkeeper.com/healthgraph)
* [Fitbit](http://dev.fitbit.com/)
* [Fat Secret](http://platform.fatsecret.com/api/)
* [Withings](http://oauth.withings.com/api)
* [Microsoft HealthVault](https://developer.healthvault.com/)
* [Jawbone UP](https://jawbone.com/up/developer)

The above links point to the developer website of each API. You'll need to visit these websites to obtain 
authentication credentials for each of the shims you want to enable.  

If any of links are incorrect or out of date, please [submit an issue](https://github.com/openmhealth/omh-shims/issues) to let us know. 
  

### Technical requirements

1. You must have a [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index-jsp-138363.html/) or higher JDK installed. 
1. A running [MongoDB](http://http://docs.mongodb.org/manual/) installation is required.
1. [Gradle](http://www.gradle.org/) or [Maven](http://maven.apache.org/) is required to build the source code.  


### Installation

1. Clone this Git repository.
1. Navigate to the `src/main/resources` directory and edit the `application.yaml` file.
1. Check that the `spring:data:mongodb:uri` parameter points to your running MongoDB instance.
1. Obtain authentication credentials, typically an OAuth client ID and client secret, for each shim you'd like to run. These are obtained from the developer websites of the third-party APIs.
1. Uncomment and replace the corresponding `clientId` and `clientSecret` placeholders with your credentials.
1. Follow [these instructions](#preparing-to-use-microsoft-healthvault) to install HealthVault libraries.
1. To build and run the shim server, navigate to the project directory in a terminal. 
  1. If you're using Maven, run `mvn spring-boot:run`
  1. If using Gradle, run `gradle bootRun`
1. The server should now be running on the default port 8083. You can change the port number in the `application.yaml` file.

### Preparing to use Microsoft HealthVault
    
The Microsoft HealthVault shim has dependencies which can't be automatically downloaded from public servers, at least 
not yet. To integrate with HealthVault,

1. Download the HealthVault Java Library version [R1.6.0](https://healthvaultjavalib.codeplex.com/releases/view/125355) archive.
1. Extract the archive.
1. Navigate to the extracted directory in a terminal.
1. Run `mvn install`
  
This will make the HealthVault libraries available to both Maven and Gradle.  

### Authorizing a data source

In order to read data from a third-party we must initiate the OAuth process and authorize data from a third-party account.

To initiate the OAuth process, do the following:
 
1. Go to the URL `http://localhost:8083/authorize/SHIM_NAME?username=UNIQUE_IDENTIFIER` in a browser or make a `GET` request
  using `curl` or some other tool.
  * The `SHIM_NAME` should be one of the names listed [below](#available-shims-and-endpoints). 
  * The `UNIQUE_IDENTIFIER` can be any unique string you'd like to use. 
1. In the JSON response, find the value `authorizationUrl` and open this URL in a new browser window. 
You should be redirected to the third-party website where you can login and authorize access to your third-party user account. 
1. Once authorized, you should be redirected to `http://localhost:8083/authorize/SHIM_NAME/callback` and you'll see an approved JSON response.

### Reading data
Now you can pull data from the third party's available end points by going to
 
**Raw Data**  
*http://localhost:8083/data/SHIM_NAME/END_POINT?username=UNIQUE_IDENTIFIER&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd*

**Open mHealth Compliant Data**  
*http://localhost:8083/data/SHIM_NAME/END_POINT?username=UNIQUE_IDENTIFIER&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize=true*

### Available shims and end points

The data read urls are constructed as follows: http://localhost:8083/data/**fitbit**/**weight**?username=UNIQUE_IDENTIFIER&dateStart=yyyy-MM-dd&dateEnd=yyyy-MM-dd&normalize=true

* fitbit
    * weight 
    * heart
    * blood_pressure
    * blood_glucose
    * steps
    * activity
* healtvault
    * activity 
    * blood_pressure
    * blood_glucose
    * height
    * weight
* withings
    * body 
    * activity
    * intraday
    * sleep    
* runkeeper
    * activity
    * weight  
* jawbone
    * body
    * sleep
    * workouts
    * moves